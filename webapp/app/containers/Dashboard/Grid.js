import React, { PropTypes } from 'react'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router'
import echarts from 'echarts/lib/echarts'

import Container from '../../components/Container'
import WidgetForm from './WidgetForm'
import Workbench from '../Widget/Workbench'
import ChartUnit from './ChartUnit'
import { Responsive, WidthProvider } from 'react-grid-layout'
import Row from 'antd/lib/row'
import Col from 'antd/lib/col'
import Button from 'antd/lib/button'
import Icon from 'antd/lib/icon'
import Tooltip from 'antd/lib/tooltip'
import Modal from 'antd/lib/modal'
import Popconfirm from 'antd/lib/popconfirm'
import Breadcrumb from 'antd/lib/breadcrumb'
import Form from 'antd/lib/form'
import InputNumber from 'antd/lib/input-number'
import Select from 'antd/lib/select'
const FormItem = Form.Item
const Option = Select.Option

import { promiseDispatcher } from '../../utils/reduxPromisation'
import { loadDashboardDetail, addDashboardItem, editDashboardItem, editDashboardItems, deleteDashboardItem, clearCurrentDashboard } from './actions'
import { makeSelectCurrentDashboard, makeSelectCurrentItems } from './selectors'
import { loadWidgets, loadWidgetlibs } from '../Widget/actions'
import { loadBizlogics, loadBizdatas } from '../Bizlogic/actions'
import { makeSelectWidgets, makeSelectWidgetlibs } from '../Widget/selectors'
import { makeSelectBizlogics } from '../Bizlogic/selectors'
import { makeSelectLoginUser } from '../App/selectors'
import chartOptionsGenerator from '../Widget/chartOptionsGenerator'
import { initializePosition, changePosition, diffPosition } from './localPositionOperator'

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

      currentItemsByUserRole: [],
      modifiedItems: false,
      formType: '',
      formVisible: false,
      modalLoading: false,
      selectedWidget: 0,
      triggerType: 'manual',
      widgetFormStep: 0,
      editPositionSign: false,

      workbenchWidgetItem: 0,
      workbenchWidget: null,
      workbenchVisible: false,

      olapVisible: false,

      flattables: {}
    }
    this.frequent = {}
  }

  componentWillMount () {
    const {
      onLoadWidgets,
      onLoadWidgetlibs,
      onLoadBizlogics,
      onLoadDashboardDetail,
      params,
      loginUser
    } = this.props
    this.charts = {}

    if (loginUser.admin) {
      onLoadBizlogics()
    }

    Promise.all([
      onLoadWidgets(),
      onLoadWidgetlibs(),
      onLoadDashboardDetail(params.id)
    ])
      .then((values) => {
        const dashboard = values[2]
        if (dashboard.widgets) {
          dashboard.widgets.forEach(item => {
            this.renderChart(item.id, item.widget_id)
            this.setFrequent(item.id, item.widget_id)
          })
        }
      })
  }

  componentWillReceiveProps (props) {
    const { loginUser, currentDashboard, currentItems } = props
    const { modifiedItems } = this.state
    if (currentItems) {
      const currentItemsByUserRole = initializePosition(loginUser, currentDashboard, currentItems)
      if (!modifiedItems) {
        this.state.modifiedItems = currentItemsByUserRole.map(item => Object.assign({}, item))
      }
      this.state.currentItemsByUserRole = currentItemsByUserRole
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

  renderChart = (id, widgetId, olap, offset, limit, callback) => {
    const {
      widgets,
      widgetlibs,
      onLoadBizdatas
    } = this.props

    const widget = widgets.find(w => w.id === widgetId)
    const widgetlib = widgetlibs.find(wl => wl.id === widget.widgetlib_id)

    let domId = `widget_${id}`
    let currentChart = this.charts[domId]

    if (widgetlib.type !== 'table') {
      domId = `widget_${id}`
      currentChart = this.charts[domId]

      if (currentChart) {
        currentChart.dispose()
      }
      currentChart = echarts.init(document.getElementById(domId))
      currentChart.showLoading('default', { color: '#8BC34A' })
      this.charts[domId] = currentChart
    }

    onLoadBizdatas(widget.flatTable_id, olap || widget.olap_sql, offset, limit)
      .then((resultset) => {
        if (widgetlib.type !== 'table') {
          const chartOptions = chartOptionsGenerator({
            dataSource: resultset.dataSource,
            chartInfo: widgetlib,
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
        } else {
          this.setState({
            flattables: Object.assign({}, this.state.flattables, {
              [`table_${id}`]: resultset
            })
          })
          if (callback) {
            callback()
          }
        }
      })
  }

  setFrequent = (id, widgetId) => {
    const { currentItems } = this.props
    const dashboardItem = currentItems.find(ci => ci.id === Number(id))

    let intervalId = `widget_${id}`
    let currentFrequent = this.frequent[intervalId]

    if (currentFrequent) {
      clearInterval(currentFrequent)
    }

    if (dashboardItem.trigger_type === 'frequent') {
      currentFrequent = setInterval(() => {
        this.renderChart(id, widgetId)
      }, Number(dashboardItem.trigger_params) * 1000)

      this.frequent[intervalId] = currentFrequent
    }
  }

  onLayoutChange = (layout, layouts) => {
    // setTimtout 中 setState 会被同步执行
    setTimeout(() => {
      const { currentItemsByUserRole, modifiedItems } = this.state
      const newModifiedItems = changePosition(modifiedItems, layout, (item) => {
        this.renderChart(item.i, item.widget_id)
        this.setFrequent(item.i, item.widget_id)
      })

      this.setState({
        modifiedItems: newModifiedItems,
        editPositionSign: diffPosition(currentItemsByUserRole, newModifiedItems)
      })
    })
  }

  diffLayout = (prev, current, update, rerender) => {
    current.forEach((item, index) => {
      const prevItem = prev[index]
      if (prevItem.x !== item.x || prevItem.y !== item.y) {
        update(prevItem, item)
      }
      if (prevItem.w !== item.w || prevItem.h !== item.h) {
        update(prevItem, item)
        rerender(prevItem)
      }
    })
  }

  showAdd = () => {
    this.setState({
      formType: 'add',
      formVisible: true
    })
  }

  showEdit = (id) => () => {
    const dashboardItem = this.props.currentItems.find(c => c.id === Number(id))
    this.setState({
      formType: 'edit',
      formVisible: true,
      selectedWidget: dashboardItem.widget_id,
      triggerType: dashboardItem.trigger_type
    }, () => {
      this.widgetForm.setFieldsValue({
        id: dashboardItem.id,
        trigger_type: dashboardItem.trigger_type,
        trigger_params: dashboardItem.trigger_params
      })
    })
  }

  hideWidgetForm = () => {
    this.setState({
      modalLoading: false,
      formVisible: false,
      selectedWidget: 0,
      triggerType: 'manual',
      widgetFormStep: 0,
      workbenchWidgetItem: 0,
      workbenchWidget: null,
      workbenchVisible: false
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

  changeFormStep = (sign) => () => {
    this.setState({
      widgetFormStep: sign
    })
  }

  onModalOk = () => {
    const { currentDashboard } = this.props
    const { modifiedItems, selectedWidget, formType } = this.state

    const formdata = this.widgetForm.getFieldsValue()

    const predictPosYArr = modifiedItems.map(wi => wi.y + wi.h)

    const item = {
      widget_id: selectedWidget,
      dashboard_id: currentDashboard.id,
      position_x: 0,
      position_y: predictPosYArr.length ? Math.max(...predictPosYArr) : 0,
      width: 4,
      length: 4,
      trigger_type: formdata.trigger_type,
      trigger_params: `${formdata.trigger_params}`
    }

    this.setState({ modalLoading: true })

    if (formType === 'add') {
      this.props.onAddDashboardItem(currentDashboard.id, item)
        .then(dashboardItem => {
          modifiedItems.push({
            x: dashboardItem.position_x,
            y: dashboardItem.position_y,
            w: dashboardItem.width,
            h: dashboardItem.length,
            i: `${dashboardItem.id}`,
            widget_id: dashboardItem.widget_id
          })
          this.renderChart(dashboardItem.id, dashboardItem.widget_id)
          this.setFrequent(dashboardItem.id, dashboardItem.widget_id)
          this.hideWidgetForm()
        })
    } else {
      this.props.onEditDashboardItem(
        currentDashboard.id,
        Object.assign({}, item, {
          id: formdata.id
        })
      )
        .then(() => {
          this.renderChart(formdata.id, selectedWidget)
          this.setFrequent(formdata.id, selectedWidget)
          this.hideWidgetForm()
        })
    }
  }

  editDashboardItems = () => {
    const {
      loginUser,
      currentDashboard,
      currentItems,
      onEditDashboardItems
    } = this.props
    const { modifiedItems } = this.state

    const changedItems = currentItems.map((item, index) => {
      const modifiedItem = modifiedItems[index]
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
      localStorage.setItem(`${loginUser.id}_${currentDashboard.id}_position`, JSON.stringify(modifiedItems))
      this.setState({ editPositionSign: false })
    }
  }

  deleteDashboardItem = (id) => () => {
    this.props.onDeleteDashboardItem(Number(id))
      .then(() => {
        const { modifiedItems } = this.state
        modifiedItems.splice(modifiedItems.findIndex(mi => mi.id === id), 1)
        if (this.charts[`widget_${id}`]) {
          this.charts[`widget_${id}`].dispose()
        }
      })
  }

  showWorkbench = (dashboardItemByUserRole) => () => {
    const widget = this.props.widgets.find(w => w.id === dashboardItemByUserRole.widget_id)
    this.setState({
      workbenchmodifiedItem: dashboardItemByUserRole.i,
      workbenchWidget: widget,
      workbenchVisible: true
    })
  }

  hideWorkbench = () => {
    const { modifiedItems, workbenchWidgetItem } = this.state
    const item = modifiedItems.find(wi => wi.i === workbenchWidgetItem)
    this.renderChart(item.i, item.widget_id)
    this.setFrequent(item.i, item.widget_id)
    this.hideWidgetForm()
  }

  syncBizdatas = (dashboardItemByUserRole) => () => {
    this.renderChart(dashboardItemByUserRole.i, dashboardItemByUserRole.widget_id)
  }

  showOlapForm = (dashboardItem) => () => {
    this.setState({
      olapVisible: true
    })
  }

  hideOlapForm = () => {
    this.setState({
      olapVisible: false
    })
  }

  render () {
    const {
      currentDashboard,
      loginUser,
      bizlogics,
      widgets,
      widgetlibs,
      onAddDashboardItem
    } = this.props

    const {
      cols,
      mounted,
      currentItemsByUserRole,
      formType,
      formVisible,
      modalLoading,
      selectedWidget,
      triggerType,
      widgetFormStep,
      editPositionSign,
      workbenchWidget,
      workbenchVisible,
      olapVisible,
      flattables
    } = this.state

    let grids

    if (widgets && widgetlibs) {
      let layouts = {
        lg: []
      }
      let itemblocks = []

      currentItemsByUserRole.forEach(item => {
        const widget = widgets.find(w => w.id === item.widget_id)
        const widgetlib = widgetlibs.find(wl => wl.id === widget.widgetlib_id)

        layouts.lg.push({
          x: item.x,
          y: item.y,
          w: item.w,
          h: item.h,
          i: item.i
        })

        let editButton = ''
        let widgetButton = ''
        let deleteButton = ''

        if (loginUser.admin) {
          editButton = (
            <Tooltip title="基本信息">
              <Icon type="edit" onClick={this.showEdit(item.i)} />
            </Tooltip>
          )
          widgetButton = (
            <Tooltip title="Widget信息">
              <Icon type="setting" onClick={this.showWorkbench(item)} />
            </Tooltip>
          )
          deleteButton = (
            <Popconfirm
              title="确定删除？"
              placement="bottom"
              onConfirm={this.deleteDashboardItem(item.i)}
            >
              <Tooltip title="删除">
                <Icon type="delete" />
              </Tooltip>
            </Popconfirm>
          )
        }

        const resultset = flattables[`table_${item.i}`]
        const dataSource = resultset ? resultset.dataSource : []
        const dataTypes = resultset ? resultset.types : []
        const pagination = resultset
          ? {
            pageSize: resultset.pageSize,
            current: resultset.pageIndex,
            total: resultset.total,
            showSizeChanger: true
          } : false

        itemblocks.push((
          <div key={item.i} className={styles.gridItem}>
            <h4 className={styles.title}>
              {widget.name}
            </h4>
            <div className={styles.tools}>
              <Tooltip title="移动">
                <i className={`${styles.move} iconfont icon-move1`} />
              </Tooltip>
              {editButton}
              {widgetButton}
              <Tooltip title="查询">
                <Icon type="search" onClick={this.showOlapForm(item)} />
              </Tooltip>
              <Tooltip title="同步数据">
                <Icon type="reload" onClick={this.syncBizdatas(item)} />
              </Tooltip>
              {deleteButton}
            </div>
            <ChartUnit
              id={item.i}
              dataSource={dataSource || []}
              dataTypes={dataTypes}
              pagination={pagination}
              chartInfo={widgetlib}
              chartParams={widget}
              x={item.x}
              y={item.y}
              w={item.w}
              g={item.h}
              onTableSearch={this.renderChart}
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

    const modalButtons = widgetFormStep
      ? [
        <Button
          key="back"
          size="large"
          onClick={this.changeFormStep(0)}>
          上一步
        </Button>,
        <Button
          key="submit"
          size="large"
          type="primary"
          loading={modalLoading}
          disabled={modalLoading}
          onClick={this.onModalOk}>
          保 存
        </Button>
      ]
      : [
        <Button
          key="forward"
          size="large"
          type="primary"
          disabled={!selectedWidget}
          onClick={this.changeFormStep(1)}>
          下一步
        </Button>
      ]

    let savePosButton = ''
    let addButton = ''

    if (editPositionSign) {
      savePosButton = (
        <Button
          size="large"
          style={{marginRight: '5px'}}
          onClick={this.editDashboardItems}
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
          onClick={this.showAdd}
        >
          新 增
        </Button>
      )
    }

    return (
      <Container>
        <Container.Title>
          <Row>
            <Col md={18} sm={12}>
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
            <Col md={6} sm={12} className={utilStyles.textAlignRight}>
              {savePosButton}
              {addButton}
            </Col>
          </Row>
        </Container.Title>
        {grids}
        <div className={styles.gridBottom} />
        <Modal
          title={`${formType === 'add' ? '新增' : '修改'} Widget`}
          wrapClassName="ant-modal-large"
          visible={formVisible}
          footer={modalButtons}
          onCancel={this.hideWidgetForm}
        >
          <WidgetForm
            type={formType}
            widgets={widgets || []}
            selectedWidget={selectedWidget}
            triggerType={triggerType}
            step={widgetFormStep}
            onWidgetSelect={this.widgetSelect}
            onTriggerTypeSelect={this.triggerTypeSelect}
            onAddDashboardItem={onAddDashboardItem}
            onClose={this.hideWidgetForm}
            ref={f => { this.widgetForm = f }}
          />
        </Modal>
        <Modal
          title="Widget 详情"
          wrapClassName={`ant-modal-xlarge ${widgetStyles.workbenchWrapper}`}
          visible={workbenchVisible}
          onCancel={this.hideWidgetForm}
          footer={false}
          maskClosable={false}
        >
          <Workbench
            type={workbenchVisible ? 'edit' : ''}
            widget={workbenchWidget}
            bizlogics={bizlogics || []}
            widgetlibs={widgetlibs || []}
            onClose={this.hideWorkbench}
            ref={f => { this.workbenchWrapper = f }}
          />
        </Modal>
        <Modal
          title="多维查询"
          visible={olapVisible}
          onCancel={this.hideOlapForm}
        >
          <Form className={styles.olapForm}>
            <Row gutter={8}>
              <Col>
                <FormItem label="Group By" className={styles.olapFormItem}>
                  <Select
                    placeholder="Group By"
                    mode="multiple"
                  >
                    <Option value="ip">ip</Option>
                  </Select>
                </FormItem>
              </Col>
            </Row>
            <Row gutter={8}>
              <Col>
                <FormItem label="Aggregation Function" className={styles.olapFormItem}>
                  <Select
                    placeholder="Aggregation Function"
                    mode="multiple"
                  >
                    <Option value="values">Avg(values)</Option>
                  </Select>
                </FormItem>
              </Col>
            </Row>
            <Row gutter={8}>
              <Col span={16}>
                <FormItem label="Order By" className={styles.olapFormItem}>
                  <Select
                    placeholder="Order By"
                    mode="multiple"
                  >
                    <Option value="count">count</Option>
                  </Select>
                </FormItem>
              </Col>
              <Col span={8}>
                <FormItem label="Limit" className={styles.olapFormItem}>
                  <InputNumber placeholder="Limit" min={0} />
                </FormItem>
              </Col>
            </Row>
            <Row gutter={8}>
              <Col>
                <FormItem label="Filters" className={styles.olapFilters}>
                  <Select
                    placeholder="Keys"
                    mode="multiple"
                  >
                    <Option value="SMP集群2">Keys: SMP集群2</Option>
                  </Select>
                </FormItem>
                <FormItem className={styles.olapFilters}>
                  <Select
                    placeholder="Level"
                    mode="multiple"
                  >
                    <Option value="urlResp">Level: urlResp</Option>
                  </Select>
                </FormItem>
                <FormItem className={styles.olapFilters}>
                  <Select
                    placeholder="Groups"
                    mode="multiple"
                  ></Select>
                </FormItem>
              </Col>
            </Row>
          </Form>
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
  widgetlibs: PropTypes.oneOfType([
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
  onLoadWidgetlibs: PropTypes.func,
  onLoadBizlogics: PropTypes.func,
  onLoadBizdatas: PropTypes.func,
  onClearCurrentDashboard: PropTypes.func
}

const mapStateToProps = createStructuredSelector({
  currentDashboard: makeSelectCurrentDashboard(),
  currentItems: makeSelectCurrentItems(),
  widgets: makeSelectWidgets(),
  widgetlibs: makeSelectWidgetlibs(),
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
    onLoadWidgetlibs: () => promiseDispatcher(dispatch, loadWidgetlibs),
    onLoadBizlogics: () => promiseDispatcher(dispatch, loadBizlogics),
    onLoadBizdatas: (id, sql, offset, limit) => promiseDispatcher(dispatch, loadBizdatas, id, sql, offset, limit),
    onClearCurrentDashboard: () => promiseDispatcher(dispatch, clearCurrentDashboard)
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Grid)
