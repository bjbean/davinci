import React, { PropTypes, PureComponent } from 'react'

import SegmentControl from '../../components/SegmentControl'
import WidgetChart from './WidgetChart'
import Table from 'antd/lib/table'
import Icon from 'antd/lib/icon'
import Button from 'antd/lib/button'
import Input from 'antd/lib/input'

import { TABLE_HEADER_HEIGHT, COLUMN_WIDTH } from '../../globalConstants'
import styles from './Widget.less'

export class SplitView extends PureComponent {
  constructor (props) {
    super(props)
    this.state = {
      tableInitiate: false,
      chartInitiate: false,
      tableWidth: 0,
      tableHeight: 0,

      loading: false
    }
  }

  componentDidMount () {
    this.setState({
      tableWidth: this.refs.tableContainer.offsetHeight,
      tableHeight: this.refs.tableContainer.offsetHeight - TABLE_HEADER_HEIGHT
    })
  }

  componentWillUpdate (props) {
    this.state.tableInitiate = !!props.dataSource
  }

  sengmentControlChange = (val) => {
    this.setState({
      chartInitiate: /chartView$/.test(val)
    })
  }

  saveWidget = () => {
    this.setState({ loading: true })
    this.props.onSaveWidget()
      .then(() => {
        this.setState({ loading: false })
      })
      .catch(() => {
        this.setState({ loading: false })
      })
  }

  render () {
    const {
      dataSource,
      chartInfo,
      chartParams,
      tableLoading,
      olapSql,
      onOlapSqlInputChange,
      onOlapSqlQuery
    } = this.props

    const {
      tableInitiate,
      chartInitiate,
      tableWidth,
      tableHeight,
      loading
    } = this.state

    const columnKeys = dataSource && dataSource.length && Object.keys(dataSource[0])
    const columns = columnKeys
      ? columnKeys
        .filter(k => typeof dataSource[0][k] !== 'object')
        .map(k => ({
          title: k.toUpperCase(),
          dataIndex: k,
          key: k,
          width: COLUMN_WIDTH
        }))
      : []

    const predictColumnsWidth = columnKeys && columnKeys.length * COLUMN_WIDTH
    const tableWidthObj = predictColumnsWidth > tableWidth
      ? { x: predictColumnsWidth }
      : null
    const tableSize = Object.assign({}, tableWidthObj, { y: tableHeight })

    const tableContent = tableInitiate
      ? (
        <Table
          dataSource={dataSource || []}
          rowKey={s => s.id}
          columns={columns}
          pagination={false}
          scroll={tableSize}
          loading={tableLoading}
          bordered
        />
      )
      : (
        <div className={styles.containerEmpty}>
          <h3>
            <Icon type="select" /> 请选择 Bizlogic 查看数据列表
          </h3>
        </div>
      )

    const chartContent = dataSource && chartInfo && chartInitiate
      ? (
        <WidgetChart
          dataSource={dataSource || []}
          chartInfo={chartInfo}
          chartParams={chartParams}
        />
      )
      : (
        <div className={styles.containerEmpty}>
          <h3>
            <Icon type="select" /> 请选择 Bizlogic 和 Widget 类型查看图表
          </h3>
        </div>
      )

    return (
      <div className={styles.splitView}>
        <div className={styles.splitViewBody}>
          <SegmentControl onChange={this.sengmentControlChange}>
            <SegmentControl.SegmentPane
              tab={<i className="iconfont icon-table" />}
              key="tableView"
            >
              <div className={styles.tableContainer} ref="tableContainer">
                {tableContent}
              </div>
            </SegmentControl.SegmentPane>
            <SegmentControl.SegmentPane
              tab={<i className="iconfont icon-chart-bar" />}
              key="chartView"
            >
              {chartContent}
            </SegmentControl.SegmentPane>
          </SegmentControl>
        </div>
        <div className={styles.splitViewFooter}>
          <div className={styles.sqlInput}>
            <Input
              size="large"
              placeholder="Write Query SQL Here"
              value={olapSql}
              onChange={onOlapSqlInputChange}
              onPressEnter={onOlapSqlQuery}
              addonAfter={
                <Icon
                  className={styles.runSql}
                  type="play-circle-o"
                  onClick={onOlapSqlQuery}
                />
              }
            />
          </div>
          <Button
            type="primary"
            loading={loading}
            disabled={loading}
            onClick={this.saveWidget}
          >
            保存并退出
          </Button>
        </div>
      </div>
    )
  }
}

SplitView.propTypes = {
  dataSource: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.array
  ]),
  chartInfo: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.object
  ]),
  chartParams: PropTypes.object,
  tableLoading: PropTypes.bool,
  olapSql: PropTypes.string,
  onSaveWidget: PropTypes.func,
  onOlapSqlInputChange: PropTypes.func,
  onOlapSqlQuery: PropTypes.func
}

export default SplitView
