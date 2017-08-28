import React, { PropTypes, PureComponent } from 'react'
import Animate from 'rc-animate'

import DashboardItemControlPanel from './DashboardItemControlPanel'
import DashboardItemControlForm from './DashboardItemControlForm'
import SharePanel from '../../../components/SharePanel'

import Chart from './Chart'
import Icon from 'antd/lib/icon'
import Tooltip from 'antd/lib/tooltip'
import Popconfirm from 'antd/lib/popconfirm'
import Popover from 'antd/lib/popover'

import styles from '../Dashboard.less'

export class DashboardItem extends PureComponent {
  constructor (props) {
    super(props)
    this.state = {
      info: {},
      loading: false,
      controlPanelVisible: false
    }
  }

  componentDidMount () {
    const {
      item,
      onInitChart
    } = this.props

    this.setState({ loading: true })

    onInitChart('rerender', item)
      .then(resultset => {
        this.setState({
          info: resultset,
          loading: false
        })
      })
  }

  onSyncBizdatas = () => {
    const {
      item,
      onRenderChart
    } = this.props

    this.setState({ loading: true })

    onRenderChart('refresh', item)
      .then(resultset => {
        this.setState({
          info: resultset,
          loading: false
        })
      })
  }

  onTableSearch = (filters, params, sorts, offset, limit) =>
    this.doSearch('refresh', filters, params, sorts, offset, limit)

  onControlSearch = (filters, params, sorts, offset, limit) =>
    this.doSearch('rerender', filters, params, sorts, offset, limit)

  doSearch = (renderType, filters, params, sorts, offset, limit) => {
    const {
      item,
      onRenderChart
    } = this.props

    this.setState({ loading: true })

    return onRenderChart(renderType, item, filters, params, sorts, offset, limit)
      .then(resultset => {
        this.setState({
          info: resultset,
          loading: false
        })
      })
  }

  toggleControlPanel = () => {
    this.setState({
      controlPanelVisible: !this.state.controlPanelVisible
    })
  }

  render () {
    const {
      item,
      w,
      h,
      widget,
      chartInfo,
      isAdmin,
      onShowEdit,
      onShowWorkbench,
      onShowFiltersForm,
      onDeleteDashboardItem
    } = this.props

    const {
      info,
      loading,
      controlPanelVisible
    } = this.state

    let editButton = ''
    let widgetButton = ''
    let shareButton = ''
    let deleteButton = ''

    if (isAdmin) {
      editButton = (
        <Tooltip title="基本信息">
          <Icon type="edit" onClick={onShowEdit(item)} />
        </Tooltip>
      )
      widgetButton = (
        <Tooltip title="Widget信息">
          <Icon type="setting" onClick={onShowWorkbench(item, widget)} />
        </Tooltip>
      )
      shareButton = (
        <Tooltip title="分享">
          <Popover placement="bottomRight" content={<SharePanel id={widget.id} type="widget" />} trigger="click">
            <Icon type="share-alt" />
          </Popover>
        </Tooltip>
      )
      deleteButton = (
        <Popconfirm
          title="确定删除？"
          placement="bottom"
          onConfirm={onDeleteDashboardItem(item.id)}
        >
          <Tooltip title="删除">
            <Icon type="delete" />
          </Tooltip>
        </Popconfirm>
      )
    }

    const controls = widget.query_params
      ? JSON.parse(widget.query_params).filter(c => c.type)
      : []
    const controlPanelHandle = controls.length
      ? (
        <Tooltip title="选择参数">
          <Icon
            type={controlPanelVisible ? 'up-square-o' : 'down-square-o'}
            onClick={this.toggleControlPanel}
          />
        </Tooltip>
      ) : ''

    const controlPanelTransitionName = {
      enter: styles.controlPanelEnter,
      enterActive: styles.controlPanelEnterActive,
      leave: styles.controlPanelLeave,
      leaveActive: styles.controlPanelLeaveActive
    }

    return (
      <div className={styles.gridItem}>
        <h4 className={styles.title}>
          {controlPanelHandle}
          {widget.name}
        </h4>
        <div className={styles.tools}>
          <Tooltip title="移动">
            <i className={`${styles.move} iconfont icon-move1`} />
          </Tooltip>
          {editButton}
          {widgetButton}
          <Tooltip title="条件查询">
            <Icon type="search" onClick={onShowFiltersForm(item, info.keys || [], info.types || [])} />
          </Tooltip>
          <Tooltip title="同步数据">
            <Icon type="reload" onClick={this.onSyncBizdatas} />
          </Tooltip>
          {shareButton}
          {deleteButton}
        </div>
        <Animate
          showProp="show"
          transitionName={controlPanelTransitionName}
        >
          <DashboardItemControlPanel show={controlPanelVisible}>
            <DashboardItemControlForm
              controls={controls}
              onSearch={this.onControlSearch}
              onHide={this.toggleControlPanel}
            />
          </DashboardItemControlPanel>
        </Animate>
        <Chart
          id={`${item.id}`}
          w={w}
          h={h}
          data={info}
          loading={loading}
          chartInfo={chartInfo}
          chartParams={widget}
          onTableSearch={this.onTableSearch}
        />
      </div>
    )
  }
}

DashboardItem.propTypes = {
  item: PropTypes.object,
  w: PropTypes.number,
  h: PropTypes.number,
  widget: PropTypes.object,
  chartInfo: PropTypes.object,
  isAdmin: PropTypes.bool,
  onInitChart: PropTypes.func,
  onRenderChart: PropTypes.func,
  onShowEdit: PropTypes.func,
  onShowWorkbench: PropTypes.func,
  onShowFiltersForm: PropTypes.func,
  onDeleteDashboardItem: PropTypes.func
}

export default DashboardItem
