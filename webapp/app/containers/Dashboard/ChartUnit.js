import React, { PropTypes, PureComponent } from 'react'
import classnames from 'classnames'

import SearchFilterDropdown from '../../components/SearchFilterDropdown'
import NumberFilterDropdown from '../../components/NumberFilterDropdown'
import DateFilterDropdown from '../../components/DateFilterDropdown'
import Table from 'antd/lib/table'

import { TABLE_HEADER_HEIGHT, TABLE_PAGINATION_HEIGHT, COLUMN_WIDTH } from '../../globalConstants'
import utilStyles from '../../assets/less/util.less'
import styles from './Dashboard.less'

export class ChartUnit extends PureComponent {
  constructor (props) {
    super(props)
    this.state = {
      sortedInfo: {},
      filterDropdownVisibles: {},
      filterValues: {},
      pagination: {},
      loading: true,

      tableWidth: 0,
      tableHeight: 0
    }
  }

  componentDidMount () {
    this.updateTableSize()
  }

  componentWillUpdate (nextProps) {
    if (nextProps.dataSource.length && !Object.keys(this.state.filterValues).length) {
      this.state.filterValues = Object
        .keys(nextProps.dataSource[0])
        .reduce((rdc, k) => {
          rdc[k] = {
            from: '',
            to: ''
          }
          return rdc
        }, {})
      this.state.loading = false
    }
    this.state.pagination = nextProps.pagination
      ? Object.assign({}, nextProps.pagination)
      : false
  }

  componentDidUpdate () {
    this.updateTableSize()
  }

  updateTableSize () {
    this.setState({
      tableWidth: this.refs.block.offsetHeight,
      tableHeight: this.refs.block.offsetHeight - TABLE_HEADER_HEIGHT - TABLE_PAGINATION_HEIGHT
    })
  }

  handleTableChange = (pagination, filters, sorter) => {
    const { id, chartParams } = this.props

    this.setState({
      pagination: pagination,
      sortedInfo: sorter
    }, () => {
      this.onLoadData(id, chartParams.id)
    })
  }

  onSearchInputChange = (columnName) => (e) => {
    const filterValues = this.state.filterValues
    this.setState({
      filterValues: Object.assign({}, filterValues, {
        [columnName]: {
          from: e.target.value,
          to: filterValues[columnName].to
        }
      })
    })
  }

  onNumberInputChange = (columnName, dir) => (e) => {
    const filterValues = this.state.filterValues
    const val = e.target.value
    this.setState({
      filterValues: Object.assign({}, filterValues, {
        [columnName]: {
          from: dir === 'from' ? (isNaN(val) ? '' : val) : filterValues[columnName].from,
          to: dir === 'to' ? (isNaN(val) ? '' : val) : filterValues[columnName].to
        }
      })
    })
  }

  onRangePickerChange = (columnName, id, widgetId) => (dates, dateStrings) => {
    const filterValues = this.state.filterValues
    this.setState({
      filterValues: Object.assign({}, filterValues, {
        [columnName]: {
          from: dateStrings[0],
          to: dateStrings[1]
        }
      })
    }, () => {
      this.onLoadData(id, widgetId)
    })
  }

  onSearch = (id, widgetId) => () => {
    this.onLoadData(id, widgetId)
  }

  onLoadData = (id, widgetId) => {
    const {
      sortedInfo,
      filterValues,
      pagination
    } = this.state

    let olap = 'select * from table'
    let where = []
    let order = []
    let limit = pagination.pageSize
    let offset = (pagination.current - 1) * limit

    const sortedInfoKeys = Object.keys(sortedInfo)
    const filterValueKeys = Object.keys(filterValues)

    if (sortedInfoKeys.length) {
      const orderStr = sortedInfo['order']
      order.push(`${sortedInfo['columnKey']} ${orderStr.substring(0, orderStr.length - 3)}`)
    }

    filterValueKeys.forEach(k => {
      const pair = filterValues[k]

      if (pair.to) {
        if (pair.from) {
          where.push(`(${k} between '${pair.from}' and '${pair.to}')`)
        } else {
          where.push(`(${k} <= '${pair.to}')`)
        }
      } else {
        if (pair.from) {
          where.push(`(${k} like '%${pair.from}%')`)
        }
      }
    })

    if (where.length) {
      olap = olap.concat(` where ${where.join(` and `)}`)
    }
    if (order.length) {
      olap = olap.concat(` order by ${order.join(',')}`)
    }

    this.setState({ loading: true })
    this.props.onTableSearch(id, widgetId, olap, offset, limit, () => {
      this.setState({ loading: false })
    })
  }

  render () {
    const {
      id,
      dataSource,
      dataTypes,
      pagination,
      chartInfo,
      chartParams
      // x,
      // y,
      // w,
      // h
    } = this.props

    const {
      sortedInfo,
      tableWidth,
      tableHeight,
      filterDropdownVisibles,
      filterValues,
      loading
    } = this.state

    let columnKeys = null
    let columnTypes = null

    const params = JSON.parse(chartParams.chart_params)
    const { dimensionColumns, metricColumns } = params

    if (dimensionColumns && dimensionColumns.length ||
        metricColumns && metricColumns.length) {
      if (dataSource.length) {
        columnKeys = Array.from(dimensionColumns).concat(Array.from(metricColumns))

        const fullKeys = Object.keys(dataSource[0])
        columnTypes = dimensionColumns.map(dc => dataTypes[fullKeys.indexOf(dc)])
          .concat(metricColumns.map(mc => dataTypes[fullKeys.indexOf(mc)]))
      }
    } else {
      if (dataSource.length) {
        columnKeys = Object.keys(dataSource[0])
        columnTypes = dataTypes
      }
    }

    const columns = columnKeys
      ? columnKeys
        .filter(k => typeof dataSource[0][k] !== 'object')
        .map((k, index) => {
          const filterValue = filterValues[k] || {}
          const columnType = columnTypes[index]

          const isNumber = ['INT', 'BIGINT', 'DOUBLE']
          const isDatetime = ['DATE']

          let filterDropdown = ''

          if (isNumber.indexOf(columnType) >= 0) {
            filterDropdown = (
              <NumberFilterDropdown
                from={filterValue.from}
                to={filterValue.to}
                onFromChange={this.onNumberInputChange(k, 'from')}
                onToChange={this.onNumberInputChange(k, 'to')}
                onSearch={this.onSearch(id, chartParams.id)}
              />
            )
          } else if (isDatetime.indexOf(columnType) >= 0) {
            filterDropdown = (
              <DateFilterDropdown
                from={filterValue.from}
                to={filterValue.to}
                onChange={this.onRangePickerChange(k, id, chartParams.id)}
              />
            )
          } else {
            filterDropdown = (
              <SearchFilterDropdown
                columnName={k}
                filterValue={filterValues[k] === undefined ? '' : filterValues[k].from}
                onSearchInputChange={this.onSearchInputChange(k)}
                onSearch={this.onSearch(id, chartParams.id)}
              />
            )
          }

          const dimensionClass = classnames({
            [styles.dimension]: dimensionColumns && dimensionColumns.indexOf(k) === dimensionColumns.length - 1
          })

          return {
            title: k.toUpperCase(),
            dataIndex: k,
            key: k,
            width: COLUMN_WIDTH,
            className: dimensionClass,
            sorter: true,
            sortOrder: sortedInfo.columnKey === k && sortedInfo.order,
            filterDropdown: filterDropdown,
            filterDropdownVisible: filterDropdownVisibles[k] === undefined ? false : filterDropdownVisibles[k],
            onFilterDropdownVisibleChange: visible => {
              this.setState({
                filterDropdownVisibles: Object.assign({}, filterDropdownVisibles, {
                  [k]: visible
                })
              })
            }
          }
        })
      : []

    const predictColumnsWidth = columnKeys && columnKeys.length * COLUMN_WIDTH
    const tableWidthObj = predictColumnsWidth > tableWidth
      ? { x: predictColumnsWidth }
      : null
    const tableSize = Object.assign({}, tableWidthObj, { y: tableHeight })

    const isTable = chartInfo.type === 'table'

    const chartClass = classnames({
      [utilStyles.hide]: isTable
    })

    const tableContent = isTable
      ? (
        <Table
          className={styles.tableBlock}
          dataSource={dataSource}
          rowKey={s => s.id}
          columns={columns}
          pagination={pagination}
          loading={loading}
          scroll={tableSize}
          onChange={this.handleTableChange}
          bordered
        />
      ) : ''

    return (
      <div className={styles.block} ref="block">
        <div className={`${styles.chartBlock} ${chartClass}`} id={`widget_${id}`}></div>
        {tableContent}
      </div>
    )
  }
}

ChartUnit.propTypes = {
  id: PropTypes.string,
  // x: PropTypes.number,
  // y: PropTypes.number,
  // w: PropTypes.number,
  // h: PropTypes.number,
  dataSource: PropTypes.array,
  dataTypes: PropTypes.array,
  pagination: PropTypes.oneOfType([
    PropTypes.object,
    PropTypes.bool
  ]),
  chartInfo: PropTypes.object,
  chartParams: PropTypes.object,
  onTableSearch: PropTypes.func
}

export default ChartUnit
