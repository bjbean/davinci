import React, { PropTypes } from 'react'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router'
import echarts from 'echarts/lib/echarts'

import Container from '../../components/Container'
import DashboardItemForm from './components/DashboardItemForm'
import Workbench from '../Widget/Workbench'
import DashboardItem from './components/DashboardItem'
import DashboardItemFilters from './components/DashboardItemFilters'
import { Responsive, WidthProvider } from 'react-grid-layout'
import Row from 'antd/lib/row'
import Col from 'antd/lib/col'
import Button from 'antd/lib/button'
import Modal from 'antd/lib/modal'
import Breadcrumb from 'antd/lib/breadcrumb'
import Popover from 'antd/lib/popover'
import Input from 'antd/lib/input'
import Icon from 'antd/lib/icon'

import widgetlibs from '../../assets/json/widgetlib'
import { promiseDispatcher } from '../../utils/reduxPromisation'
import { loadDashboardDetail, addDashboardItem, editDashboardItem, editDashboardItems, deleteDashboardItem, clearCurrentDashboard, loadDashboardShareLink, loadWidgetShareLink } from './actions'
import { makeSelectCurrentDashboard, makeSelectCurrentItems } from './selectors'
import { loadWidgets } from '../Widget/actions'
import { loadBizlogics, loadBizdatas } from '../Bizlogic/actions'
import { makeSelectWidgets } from '../Widget/selectors'
import { makeSelectBizlogics } from '../Bizlogic/selectors'
import { makeSelectLoginUser } from '../App/selectors'
import chartOptionsGenerator from '../Widget/chartOptionsGenerator'
import { initializePosition, changePosition, diffPosition } from './components/localPositionOperator'

import config, { env } from '../../globalConfig'
const shareHost = config[env].shareHost
import utilStyles from '../../assets/less/util.less'
import widgetStyles from '../Widget/Widget.less'
import styles from './Dashboard.less'

const ResponsiveReactGridLayout = WidthProvider(Responsive)

export class Grid extends React.Component {
  constructor (props) {
    super(props)
    this.state = {
      cols: { lg: 12, md: 10, sm: 6, xs: 4, xxs: 2 },
      mounted: false,

      localPositions: [],
      modifiedPositions: false,
      editPositionSign: false,
      shareLink: '',

      dashboardItemFormType: '',
      dashboardItemFormVisible: false,
      dashboardItemFormStep: 0,
      modalLoading: false,
      selectedWidget: 0,
      triggerType: 'manual',

      workbenchDashboardItem: 0,
      workbenchWidget: null,
      workbenchVisible: false,

      filtersVisible: false,
      filtersDashboardItem: 0,
      filtersKeys: null,
      filtersTypes: null
    }
    this.frequent = {}
  }

  componentWillMount () {
    const {
      onLoadWidgets,
      onLoadBizlogics,
      onLoadDashboardDetail,
      params,
      loginUser
    } = this.props
    this.charts = {}

    if (loginUser.admin) {
      onLoadBizlogics()
    }

    onLoadWidgets()
    onLoadDashboardDetail(params.id)
  }

  componentWillReceiveProps (props, states) {
    const { loginUser, currentDashboard, currentItems } = props
    const { modifiedPositions } = this.state
    if (currentItems) {
      const localPositions = initializePosition(loginUser, currentDashboard, currentItems)
      if (!modifiedPositions) {
        this.state.modifiedPositions = localPositions.map(item => Object.assign({}, item))
      }
      this.state.localPositions = localPositions
    }
  }

  componentDidMount () {
    this.setState({ mounted: true })
  }

  componentWillUnmount () {
    Object.keys(this.charts).forEach(k => {
      this.charts[k].dispose()
    })
    Object.keys(this.frequent).forEach(k => {
      clearInterval(this.frequent[k])
    })
    this.props.onClearCurrentDashboard()
  }

  renderChart = (renderType, dashboardItem, filters, sorts, offset, limit) =>
    new Promise((resolve) => {
      const {
        widgets
      } = this.props

      const widget = widgets.find(w => w.id === dashboardItem.widget_id)
      const chartInfo = widgetlibs.find(wl => wl.id === widget.widgetlib_id)

      const domId = `widget_${dashboardItem.id}`
      let currentChart = this.charts[domId]

      if (chartInfo.name !== 'table') {
        switch (renderType) {
          case 'rerender':
            if (currentChart) {
              currentChart.dispose()
            }
            currentChart = echarts.init(document.getElementById(domId))
            this.charts[domId] = currentChart
            currentChart.showLoading('default', { color: '#8BC34A' })
            break
          case 'clear':
            currentChart.clear()
            currentChart.showLoading('default', { color: '#8BC34A' })
            break
          case 'refresh':
            currentChart.showLoading('default', { color: '#8BC34A' })
            break
          default:
            break
        }
      }

      const adhocAndFilters = {
        adHoc: widget.adhoc_sql,
        manualFilters: filters
      }

      this.props.onLoadBizdatas(widget.flatTable_id, adhocAndFilters, sorts, offset, limit)
        .then((resultset) => {
          resolve(resultset)

          if (chartInfo.name !== 'table') {
            const chartOptions = chartOptionsGenerator({
              dataSource: resultset.dataSource,
              chartInfo: chartInfo,
              chartParams: Object.assign({
                id: widget.id,
                name: widget.name,
                desc: widget.desc,
                flatTable_id: widget.flatTable_id,
                widgetlib_id: widget.widgetlib_id
              }, JSON.parse(widget.chart_params))
            })
            currentChart.setOption(chartOptions)
            currentChart.hideLoading()
          }
        })
    })

  setFrequent = (dashboardItem, filters, sorts, offset, limit) => {
    let intervalId = `widget_${dashboardItem.id}`
    let currentFrequent = this.frequent[intervalId]

    if (currentFrequent) {
      clearInterval(currentFrequent)
    }

    if (dashboardItem.trigger_type === 'frequent') {
      currentFrequent = setInterval(() => {
        this.renderChart('dynamic', dashboardItem, filters, sorts, offset, limit)
      }, Number(dashboardItem.trigger_params) * 1000)

      this.frequent[intervalId] = currentFrequent
    }
  }

  renderChartAndSetFrequent = (renderType, dashboardItem, filters, sorts, offset, limit) => {
    const promise = this.renderChart(renderType, dashboardItem, filters, sorts, offset, limit)
    this.setFrequent(dashboardItem, filters, sorts, offset, limit)
    return promise
  }

  onLayoutChange = (layout, layouts) => {
    // setTimtout 中 setState 会被同步执行
    setTimeout(() => {
      const { localPositions, modifiedPositions } = this.state
      const newModifiedItems = changePosition(modifiedPositions, layout, (pos) => {
        const dashboardItem = this.props.currentItems.find(item => item.id === Number(pos.i))
        this.renderChartAndSetFrequent('rerender', dashboardItem)
      })

      this.setState({
        modifiedPositions: newModifiedItems,
        editPositionSign: diffPosition(localPositions, newModifiedItems)
      })
    })
  }

  showAddDashboardItemForm = () => {
    this.setState({
      dashboardItemFormType: 'add',
      dashboardItemFormVisible: true
    })
  }

  showEditDashboardItemForm = (dashboardItem) => () => {
    this.setState({
      dashboardItemFormType: 'edit',
      dashboardItemFormVisible: true,
      selectedWidget: dashboardItem.widget_id,
      triggerType: dashboardItem.trigger_type
    }, () => {
      this.dashboardItemForm.setFieldsValue({
        id: dashboardItem.id,
        trigger_type: dashboardItem.trigger_type,
        trigger_params: dashboardItem.trigger_params
      })
    })
  }

  hideDashboardItemForm = () => {
    this.setState({
      modalLoading: false,
      dashboardItemFormVisible: false
    })
  }

  afterDashboardItemFormClose = () => {
    this.setState({
      selectedWidget: 0,
      triggerType: 'manual',
      dashboardItemFormStep: 0
    })
  }

  widgetSelect = (id) => () => {
    this.setState({
      selectedWidget: id
    })
  }

  triggerTypeSelect = (val) => {
    this.setState({
      triggerType: val
    })
  }

  changeDashboardItemFormStep = (sign) => () => {
    this.setState({
      dashboardItemFormStep: sign
    })
  }

  saveDashboardItem = () => {
    const { currentDashboard, currentItems } = this.props
    const { modifiedPositions, selectedWidget, dashboardItemFormType } = this.state

    const formdata = this.dashboardItemForm.getFieldsValue()

    const predictPosYArr = modifiedPositions.map(wi => wi.y + wi.h)

    const newItem = {
      widget_id: selectedWidget,
      dashboard_id: currentDashboard.id,
      trigger_type: formdata.trigger_type,
      trigger_params: `${formdata.trigger_params}`
    }

    this.setState({ modalLoading: true })

    if (dashboardItemFormType === 'add') {
      const positionInfo = {
        position_x: 0,
        position_y: predictPosYArr.length ? Math.max(...predictPosYArr) : 0,
        width: 4,
        length: 4
      }

      this.props.onAddDashboardItem(currentDashboard.id, Object.assign({}, newItem, positionInfo))
        .then(dashboardItem => {
          modifiedPositions.push({
            x: dashboardItem.position_x,
            y: dashboardItem.position_y,
            w: dashboardItem.width,
            h: dashboardItem.length,
            i: `${dashboardItem.id}`
          })
          this.hideDashboardItemForm()
        })
    } else {
      const dashboardItem = currentItems.find(item => item.id === Number(formdata.id))
      const modifiedDashboardItem = Object.assign({}, dashboardItem, newItem)

      this.props.onEditDashboardItem(
        currentDashboard.id,
        modifiedDashboardItem
      )
        .then(() => {
          this.renderChartAndSetFrequent('rerender', modifiedDashboardItem)
          this.hideDashboardItemForm()
        })
    }
  }

  editDashboardItemPositions = () => {
    const {
      loginUser,
      currentDashboard,
      currentItems,
      onEditDashboardItems
    } = this.props
    const { modifiedPositions } = this.state

    const changedItems = currentItems.map((item, index) => {
      const modifiedItem = modifiedPositions[index]
      return {
        id: item.id,
        widget_id: item.widget_id,
        dashboard_id: currentDashboard.id,
        position_x: modifiedItem.x,
        position_y: modifiedItem.y,
        width: modifiedItem.w,
        length: modifiedItem.h,
        trigger_type: item.trigger_type,
        trigger_params: item.trigger_params
      }
    })

    if (loginUser.admin) {
      onEditDashboardItems(currentDashboard.id, changedItems)
        .then(() => {
          this.setState({ editPositionSign: false })
        })
    } else {
      localStorage.setItem(`${loginUser.id}_${currentDashboard.id}_position`, JSON.stringify(modifiedPositions))
      this.setState({ editPositionSign: false })
    }
  }

  deleteDashboardItem = (id) => () => {
    this.props.onDeleteDashboardItem(id)
      .then(() => {
        const { modifiedPositions } = this.state
        modifiedPositions.splice(modifiedPositions.findIndex(mi => Number(mi.i) === id), 1)
        if (this.charts[`widget_${id}`]) {
          this.charts[`widget_${id}`].dispose()
        }
        if (this.frequent[`widget_${id}`]) {
          clearInterval(this.frequent[`widget_${id}`])
        }
      })
  }

  showWorkbench = (dashboardItem, widget) => () => {
    this.setState({
      workbenchDashboardItem: dashboardItem.id,
      workbenchWidget: widget,
      workbenchVisible: true
    })
  }

  hideWorkbench = () => {
    this.setState({
      workbenchDashboardItem: 0,
      workbenchWidget: null,
      workbenchVisible: false
    })
  }

  onWorkbenchClose = () => {
    const dashboardItem = this.props.currentItems.find(item => item.id === this.state.workbenchDashboardItem)
    this.renderChartAndSetFrequent('rerender', dashboardItem)
    this.hideWorkbench()
  }

  showFiltersForm = (dashboardItem, keys, types) => () => {
    this.setState({
      filtersVisible: true,
      filtersDashboardItem: dashboardItem.id,
      filtersKeys: keys,
      filtersTypes: types
    })
  }

  hideFiltersForm = () => {
    this.setState({
      filtersVisible: false,
      filtersDashboardItem: 0,
      filtersKeys: [],
      filtersTypes: []
    })
    this.dashboardItemFilters.refs.wrappedComponent.refs.formWrappedComponent.resetTree()
  }

  doFilterQuery = (sql) => {
    // const dashboardItem = this.props.currentItems.find(item => item.id === this.state.filtersDashboardItem)
    // this.renderChart('refresh', dashboardItem, sql)
    this[`dashboardItem${this.state.filtersDashboardItem}`].onTableSearch(sql)
    this.hideFiltersForm()
  }

  doShare = () => {
    const { shareLink } = this.state
    if (!shareLink) {
      const {
        currentDashboard,
        onLoadDashboardShareLink
      } = this.props

      onLoadDashboardShareLink(currentDashboard.id)
        .then(shareInfo => {
          this.setState({
            shareLink: `${shareHost}/#share?shareInfo=${encodeURI(shareInfo)}&type=dashboard`
          })
        })
    }
  }

  handleShareInputSelect = () => {
    this.refs.shareInput.refs.input.select()
    document.execCommand('copy')
  }

  render () {
    const {
      currentDashboard,
      currentItems,
      loginUser,
      bizlogics,
      widgets,
      onLoadWidgetShareLink
    } = this.props

    const {
      cols,
      mounted,
      localPositions,
      modifiedPositions,
      shareLink,
      dashboardItemFormType,
      dashboardItemFormVisible,
      modalLoading,
      selectedWidget,
      triggerType,
      dashboardItemFormStep,
      editPositionSign,
      workbenchWidget,
      workbenchVisible,
      filtersVisible,
      filtersDashboardItem,
      filtersKeys,
      filtersTypes
    } = this.state

    let grids

    if (widgets) {
      let layouts = {
        lg: []
      }
      let itemblocks = []

      localPositions.forEach((pos, index) => {
        layouts.lg.push({
          x: pos.x,
          y: pos.y,
          w: pos.w,
          h: pos.h,
          i: pos.i
        })

        const dashboardItem = currentItems[index]
        const modifiedPosition = modifiedPositions[index]
        const widget = widgets.find(w => w.id === dashboardItem.widget_id)
        const chartInfo = widgetlibs.find(wl => wl.id === widget.widgetlib_id)

        itemblocks.push((
          <div key={pos.i}>
            <DashboardItem
              item={dashboardItem}
              w={modifiedPosition ? modifiedPosition.w : 0}
              h={modifiedPosition ? modifiedPosition.h : 0}
              widget={widget}
              chartInfo={chartInfo}
              isAdmin={loginUser.admin}
              onInitChart={this.renderChartAndSetFrequent}
              onRenderChart={this.renderChart}
              onShowEdit={this.showEditDashboardItemForm}
              onShowWorkbench={this.showWorkbench}
              onShowFiltersForm={this.showFiltersForm}
              onLoadWidgetShareLink={onLoadWidgetShareLink}
              onDeleteDashboardItem={this.deleteDashboardItem}
              ref={f => { this[`dashboardItem${pos.i}`] = f }}
            />
          </div>
        ))
      })

      grids = (
        <ResponsiveReactGridLayout
          className="layout"
          style={{marginTop: '-28px'}}
          rowHeight={30}
          margin={[20, 20]}
          cols={cols}
          layouts={layouts}
          onLayoutChange={this.onLayoutChange}
          measureBeforeMount={false}
          draggableHandle={`.${styles.move}`}
          useCSSTransforms={mounted}>
          {itemblocks}
        </ResponsiveReactGridLayout>
      )
    }

    const modalButtons = dashboardItemFormStep
      ? [
        <Button
          key="back"
          size="large"
          onClick={this.changeDashboardItemFormStep(0)}>
          上一步
        </Button>,
        <Button
          key="submit"
          size="large"
          type="primary"
          loading={modalLoading}
          disabled={modalLoading}
          onClick={this.saveDashboardItem}>
          保 存
        </Button>
      ]
      : [
        <Button
          key="forward"
          size="large"
          type="primary"
          disabled={!selectedWidget}
          onClick={this.changeDashboardItemFormStep(1)}>
          下一步
        </Button>
      ]

    let savePosButton = ''
    let addButton = ''
    let shareButton = ''
    let shareContent = ''

    if (editPositionSign) {
      savePosButton = (
        <Button
          size="large"
          style={{marginRight: '5px'}}
          onClick={this.editDashboardItemPositions}
        >
          保存位置修改
        </Button>
      )
    }

    if (loginUser.admin) {
      addButton = (
        <Button
          size="large"
          type="primary"
          icon="plus"
          style={{marginRight: '5px'}}
          onClick={this.showAddDashboardItemForm}
        >
          新 增
        </Button>
      )

      if (shareLink) {
        shareContent = (
          <Input
            className={styles.shareInput}
            ref="shareInput"
            value={shareLink}
            addonAfter={
              <span
                style={{cursor: 'pointer'}}
                onClick={this.handleShareInputSelect}
              >复制</span>
            }
            readOnly
          />
        )
      } else {
        shareContent = (
          <Icon type="loading" />
        )
      }

      shareButton = (
        <Popover placement="bottomRight" content={shareContent} trigger="click">
          <Button
            size="large"
            type="primary"
            icon="share-alt"
            onClick={this.doShare}
          >
            分 享
          </Button>
        </Popover>
      )
    }

    return (
      <Container>
        <Container.Title>
          <Row>
            <Col sm={12}>
              <Breadcrumb className={utilStyles.breadcrumb}>
                <Breadcrumb.Item>
                  <Link to="/visual/report/dashboard">
                    Dashboard
                  </Link>
                </Breadcrumb.Item>
                <Breadcrumb.Item>
                  <Link>
                    {currentDashboard && currentDashboard.name}
                  </Link>
                </Breadcrumb.Item>
              </Breadcrumb>
            </Col>
            <Col sm={12} className={utilStyles.textAlignRight}>
              {savePosButton}
              {addButton}
              {shareButton}
            </Col>
          </Row>
        </Container.Title>
        {grids}
        <div className={styles.gridBottom} />
        <Modal
          title={`${dashboardItemFormType === 'add' ? '新增' : '修改'} Widget`}
          wrapClassName="ant-modal-large"
          visible={dashboardItemFormVisible}
          footer={modalButtons}
          onCancel={this.hideDashboardItemForm}
          afterClose={this.afterDashboardItemFormClose}
        >
          <DashboardItemForm
            type={dashboardItemFormType}
            widgets={widgets || []}
            selectedWidget={selectedWidget}
            triggerType={triggerType}
            step={dashboardItemFormStep}
            onWidgetSelect={this.widgetSelect}
            onTriggerTypeSelect={this.triggerTypeSelect}
            ref={f => { this.dashboardItemForm = f }}
          />
        </Modal>
        <Modal
          title="Widget 详情"
          wrapClassName={`ant-modal-xlarge ${widgetStyles.workbenchWrapper}`}
          visible={workbenchVisible}
          onCancel={this.hideWorkbench}
          footer={false}
          maskClosable={false}
        >
          <Workbench
            type={workbenchVisible ? 'edit' : ''}
            widget={workbenchWidget}
            bizlogics={bizlogics || []}
            widgetlibs={widgetlibs}
            onClose={this.onWorkbenchClose}
            ref={f => { this.workbenchWrapper = f }}
          />
        </Modal>
        <Modal
          title="条件查询"
          wrapClassName="ant-modal-xlarge"
          visible={filtersVisible}
          onCancel={this.hideFiltersForm}
          footer={false}
        >
          <DashboardItemFilters
            loginUser={loginUser}
            itemId={filtersDashboardItem}
            keys={filtersKeys}
            types={filtersTypes}
            onQuery={this.doFilterQuery}
            ref={f => { this.dashboardItemFilters = f }}
          />
        </Modal>
      </Container>
    )
  }
}

Grid.propTypes = {
  currentDashboard: PropTypes.object,
  currentItems: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.array
  ]),
  onLoadDashboardDetail: PropTypes.func,
  onAddDashboardItem: PropTypes.func,
  onEditDashboardItem: PropTypes.func,
  onEditDashboardItems: PropTypes.func,
  onDeleteDashboardItem: PropTypes.func,
  widgets: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.array
  ]),
  bizlogics: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.array
  ]),
  loginUser: PropTypes.object,
  params: PropTypes.any,
  onLoadWidgets: PropTypes.func,
  onLoadBizlogics: PropTypes.func,
  onLoadBizdatas: PropTypes.func,
  onClearCurrentDashboard: PropTypes.func,
  onLoadDashboardShareLink: PropTypes.func,
  onLoadWidgetShareLink: PropTypes.func
}

const mapStateToProps = createStructuredSelector({
  currentDashboard: makeSelectCurrentDashboard(),
  currentItems: makeSelectCurrentItems(),
  widgets: makeSelectWidgets(),
  bizlogics: makeSelectBizlogics(),
  loginUser: makeSelectLoginUser()
})

export function mapDispatchToProps (dispatch) {
  return {
    onLoadDashboardDetail: (id) => promiseDispatcher(dispatch, loadDashboardDetail, id),
    onAddDashboardItem: (id, item) => promiseDispatcher(dispatch, addDashboardItem, id, item),
    onEditDashboardItem: (id, item) => promiseDispatcher(dispatch, editDashboardItem, id, item),
    onEditDashboardItems: (id, items) => promiseDispatcher(dispatch, editDashboardItems, id, items),
    onDeleteDashboardItem: (id) => promiseDispatcher(dispatch, deleteDashboardItem, id),
    onLoadWidgets: () => promiseDispatcher(dispatch, loadWidgets),
    onLoadBizlogics: () => promiseDispatcher(dispatch, loadBizlogics),
    onLoadBizdatas: (id, sql, sorts, offset, limit) => promiseDispatcher(dispatch, loadBizdatas, id, sql, sorts, offset, limit),
    onClearCurrentDashboard: () => promiseDispatcher(dispatch, clearCurrentDashboard),
    onLoadDashboardShareLink: (id) => promiseDispatcher(dispatch, loadDashboardShareLink, id),
    onLoadWidgetShareLink: (id) => promiseDispatcher(dispatch, loadWidgetShareLink, id)
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Grid)
