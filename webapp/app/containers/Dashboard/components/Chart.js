import React, { PropTypes, PureComponent } from 'react'
import classnames from 'classnames'

import TableChart from './TableChart'

import { TABLE_HEADER_HEIGHT, TABLE_PAGINATION_HEIGHT } from '../../../globalConstants'
import utilStyles from '../../../assets/less/util.less'
import styles from '../Dashboard.less'

export class Chart extends PureComponent {
  constructor (props) {
    super(props)
    this.state = {
      tableWidth: 0,
      tableHeight: 0
    }
  }

  componentDidMount () {
    this.updateTableSize()
  }

  componentDidUpdate () {
    this.updateTableSize()
  }

  updateTableSize () {
    this.setState({
      tableWidth: this.refs.block.offsetWidth,
      tableHeight: this.refs.block.offsetHeight - TABLE_HEADER_HEIGHT - TABLE_PAGINATION_HEIGHT
    })
  }

  render () {
    const {
      id,
      data,
      loading,
      chartInfo,
      chartParams,
      onTableSearch
    } = this.props

    const {
      tableWidth,
      tableHeight
    } = this.state

    const params = JSON.parse(chartParams.chart_params)
    const { dimensionColumns, metricColumns } = params

    const isTable = chartInfo.type === 'table'

    const chartClass = classnames({
      [utilStyles.hide]: isTable
    })

    const tableContent = isTable
      ? (
        <TableChart
          className={styles.tableBlock}
          data={data}
          loading={loading}
          dimensionColumns={dimensionColumns}
          metricColumns={metricColumns}
          width={tableWidth}
          height={tableHeight}
          onChange={onTableSearch}
          filterable
          sortable
        />
      ) : ''

    return (
      <div className={styles.block} ref="block">
        <div className={`${styles.chartBlock} ${chartClass}`} id={`widget_${id}`} />
        {tableContent}
      </div>
    )
  }
}

Chart.propTypes = {
  id: PropTypes.string,
  w: PropTypes.number,  // eslint-disable-line
  h: PropTypes.number,  // eslint-disable-line
  data: PropTypes.object,
  loading: PropTypes.bool,
  chartInfo: PropTypes.object,
  chartParams: PropTypes.object,
  onTableSearch: PropTypes.func
}

export default Chart
