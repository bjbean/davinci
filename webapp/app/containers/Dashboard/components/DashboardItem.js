import React, { PropTypes, PureComponent } from 'react'
import Animate from 'rc-animate'

import DashboardItemControlPanel from './DashboardItemControlPanel'

import Chart from './Chart'
import Icon from 'antd/lib/icon'
import Tooltip from 'antd/lib/tooltip'
import Popconfirm from 'antd/lib/popconfirm'
import Popover from 'antd/lib/popover'
import Input from 'antd/lib/input'

import config, { env } from '../../../globalConfig'
const shareHost = config[env].shareHost
import styles from '../Dashboard.less'

export class DashboardItem extends PureComponent {
  constructor (props) {
    super(props)
    this.state = {
      info: {},
      loading: false,
      shareLink: '',
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

  onTableSearch = (filters, sorts, offset, limit) => {
    const {
      item,
      onRenderChart
    } = this.props

    this.setState({ loading: true })

    return onRenderChart('refresh', item, filters, sorts, offset, limit)
      .then(resultset => {
        this.setState({
          info: resultset,
          loading: false
        })
      })
  }

  doShare = () => {
    const { shareLink } = this.state
    if (!shareLink) {
      const {
        item,
        onLoadWidgetShareLink
      } = this.props

      onLoadWidgetShareLink(item.widget_id)
        .then(shareInfo => {
          this.setState({
            shareLink: `${shareHost}/#share?shareInfo=${encodeURI(shareInfo)}&type=widget`
          })
        })
    }
  }

  handleShareInputSelect = () => {
    this.refs.shareInput.refs.input.select()
    document.execCommand('copy')
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
      shareLink,
      controlPanelVisible
    } = this.state

    let editButton = ''
    let widgetButton = ''
    let shareButton = ''
    let shareContent = ''
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
        <Tooltip title="分享">
          <Popover placement="bottomRight" content={shareContent} trigger="click">
            <Icon type="share-alt" onClick={this.doShare} />
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

    const controlPanelTransitionName = {
      enter: styles.controlPanelEnter,
      enterActive: styles.controlPanelEnterActive,
      leave: styles.controlPanelLeave,
      leaveActive: styles.controlPanelLeaveActive
    }

    return (
      <div className={styles.gridItem}>
        <h4 className={styles.title}>
          {widget.name}
          <Tooltip title="选择参数">
            <Icon
              type={controlPanelVisible ? 'up-square-o' : 'down-square-o'}
              onClick={this.toggleControlPanel}
            />
          </Tooltip>
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
          <DashboardItemControlPanel
            show={controlPanelVisible}
          />
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
  onLoadWidgetShareLink: PropTypes.func,
  onDeleteDashboardItem: PropTypes.func
}

export default DashboardItem
