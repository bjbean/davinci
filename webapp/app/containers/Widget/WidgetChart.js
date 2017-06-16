import React, { PropTypes } from 'react'
// import Table from 'antd/lib/table'
import echarts from 'echarts/lib/echarts'
import 'echarts/lib/chart/bar'
import 'echarts/lib/chart/line'
import 'echarts/lib/chart/scatter'
import 'echarts/lib/chart/pie'
import 'echarts/lib/chart/sankey'
import 'echarts/lib/chart/funnel'
import 'echarts/lib/chart/treemap'
import './temp/wordCloud'
import 'echarts/lib/component/legend'
import 'echarts/lib/component/tooltip'
import 'echarts/lib/component/toolbox'

import chartOptionsGenerator from './chartOptionsGenerator'

import styles from './Widget.less'

export class WidgetChart extends React.PureComponent {
  componentDidMount () {
    this.chart = echarts.init(document.getElementById('chartContainer'))
    this.renderChart(this.props)
  }

  componentWillUpdate (nextProps) {
    this.renderChart(nextProps)
  }

  renderChart = ({ dataSource, chartInfo, chartParams }) => {
    this.chart.clear()
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

  render () {
    return (
      <div id="chartContainer" className={styles.chartContainer}></div>
    )
  }
}

WidgetChart.propTypes = {
  dataSource: PropTypes.array,  // eslint-disable-line
  chartInfo: PropTypes.object,  // eslint-disable-line
  chartParams: PropTypes.object   // eslint-disable-line
}

export default WidgetChart
