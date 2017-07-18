import React, { PropTypes } from 'react'
import classnames from 'classnames'

import Table from 'antd/lib/table'
import echarts from 'echarts/lib/echarts'

import chartOptionsGenerator from './chartOptionsGenerator'

import { TABLE_HEADER_HEIGHT, COLUMN_WIDTH } from '../../globalConstants'
import utilStyles from '../../assets/less/util.less'
import styles from './Widget.less'

export class WidgetChart extends React.PureComponent {
  constructor (props) {
    super(props)
    this.state = {
      tableWidth: 0,
      tableHeight: 0
    }
  }

  componentDidMount () {
    this.chart = echarts.init(document.getElementById('chartContainer'))
    this.renderChart(this.props)
    this.setState({
      tableWidth: this.refs.widgetChart.offsetHeight,
      tableHeight: this.refs.widgetChart.offsetHeight - TABLE_HEADER_HEIGHT
    })
  }

  componentWillUpdate (nextProps) {
    this.renderChart(nextProps)
  }

  renderChart = ({ dataSource, chartInfo, chartParams }) => {
    this.chart.clear()
    if (chartInfo.type !== 'table') {
      const chartOptions = chartOptionsGenerator({
        dataSource,
        chartInfo,
        chartParams
      })

      switch (chartInfo.type) {
        case 'line':
        case 'bar':
        case 'scatter':
        case 'area':
          if (chartOptions.xAxis && chartOptions.series) {
            this.chart.setOption(chartOptions)
          }
          break
        default:
          if (chartOptions.series) {
            this.chart.setOption(chartOptions)
          }
          break
      }
    }
  }

  render () {
    const {
      dataSource,
      chartInfo
    } = this.props

    const {
      tableWidth,
      tableHeight
    } = this.state

    const columnKeys = dataSource.length && Object.keys(dataSource[0])
    const columns = columnKeys
      ? Object.keys(dataSource[0])
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

    const tableClass = classnames({
      [utilStyles.hide]: chartInfo.type !== 'table'
    })
    const chartClass = classnames({
      [utilStyles.hide]: chartInfo.type === 'table'
    })

    return (
      <div className={styles.widgetChart} ref="widgetChart">
        <Table
          className={`${styles.tableContainer} ${tableClass}`}
          dataSource={dataSource}
          rowKey="antDesignTableId"
          columns={columns}
          scroll={tableSize}
          bordered
        />
        <div id="chartContainer" className={`${styles.chartContainer} ${chartClass}`}></div>
      </div>
    )
  }
}

WidgetChart.propTypes = {
  dataSource: PropTypes.array,
  chartInfo: PropTypes.object,
  chartParams: PropTypes.object   // eslint-disable-line
}

export default WidgetChart
