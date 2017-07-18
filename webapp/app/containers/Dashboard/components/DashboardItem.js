import React, { PropTypes, PureComponent } from 'react'

import Chart from './Chart'
import Icon from 'antd/lib/icon'
import Tooltip from 'antd/lib/tooltip'
import Popconfirm from 'antd/lib/popconfirm'

import styles from '../Dashboard.less'

export class DashboardItem extends PureComponent {
  constructor (props) {
    super(props)
    this.state = {
      info: {},
      loading: false
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
      loading
    } = this.state

    let editButton = ''
    let widgetButton = ''
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

    return (
      <div className={styles.gridItem}>
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
            <Icon type="search" onClick={onShowFiltersForm(item, info.keys || [], info.types || [])} />
          </Tooltip>
          <Tooltip title="同步数据">
            <Icon type="reload" onClick={this.onSyncBizdatas} />
          </Tooltip>
          {deleteButton}
        </div>
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
