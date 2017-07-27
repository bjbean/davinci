import React, { PropTypes, PureComponent } from 'react'
import classnames from 'classnames'

import Table from 'antd/lib/table'
import SearchFilterDropdown from '../../../components/SearchFilterDropdown/index'
import NumberFilterDropdown from '../../../components/NumberFilterDropdown/index'
import DateFilterDropdown from '../../../components/DateFilterDropdown/index'

import { COLUMN_WIDTH } from '../../../globalConstants'
import styles from '../Dashboard.less'

export class TableChart extends PureComponent {
  constructor (props) {
    super(props)
    this.state = {
      sortedInfo: {},
      filterDropdownVisibles: {},
      filterValues: {}
    }
  }

  componentWillUpdate (nextProps) {
    if (nextProps.data.keys &&
      nextProps.data.keys.length &&
      !Object.keys(this.state.filterValues).length) {
      this.state.filterValues = nextProps.data.keys
        .reduce((rdc, k) => {
          rdc[k] = {
            from: '',
            to: ''
          }
          return rdc
        }, {})
    }
  }

  handleTableChange = (pagination, filters, sorter) => {
    this.setState({
      sortedInfo: sorter
    }, () => {
      this.onLoadData(pagination)
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

  onRangePickerChange = (columnName) => (dates, dateStrings) => {
    const filterValues = this.state.filterValues
    this.setState({
      filterValues: Object.assign({}, filterValues, {
        [columnName]: {
          from: dateStrings[0],
          to: dateStrings[1]
        }
      })
    }, () => {
      this.onLoadData()
    })
  }

  onLoadData = (newPagination) => {
    const {
      data
    } = this.props

    const {
      sortedInfo,
      filterValues
    } = this.state

    const pagination = newPagination || {
      pageSize: data.pageSize,
      current: data.pageIndex,
      total: data.total,
      showSizeChanger: true
    }

    let filterSql = ''
    let sorts = ''
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
      filterSql = filterSql.concat(where.join(` and `))
    }
    if (order.length) {
      sorts = sorts.concat(order.join(':'))
    }

    this.props.onChange(filterSql, sorts, offset, limit)
  }

  render () {
    const {
      data,
      loading,
      dimensionColumns,
      metricColumns,
      className,
      filterable,
      sortable,
      width,
      height
    } = this.props

    const {
      sortedInfo,
      filterDropdownVisibles,
      filterValues
    } = this.state

    const dataSource = data.dataSource || []
    const dataKeys = data.keys || []
    const dataTypes = data.types || []
    const pagination = data.total
      ? {
        pageSize: data.pageSize,
        current: data.pageIndex,
        total: data.total,
        showSizeChanger: true
      } : false

    let columnKeys = null
    let columnTypes = null

    if (dimensionColumns && dimensionColumns.length ||
      metricColumns && metricColumns.length) {
      columnKeys = Array.from(dimensionColumns).concat(Array.from(metricColumns))
      columnTypes = dimensionColumns.map(dc => dataTypes[dataKeys.indexOf(dc)])
        .concat(metricColumns.map(mc => dataTypes[dataKeys.indexOf(mc)]))
    } else {
      columnKeys = dataKeys
      columnTypes = dataTypes
    }

    const columns = columnKeys
      .filter(k => {
        if (dataSource.length) {
          return typeof dataSource[0][k] !== 'object'
        } else {
          return true
        }
      })
      .map((k, index) => {
        let filterDropdown = ''
        let filters = null

        if (filterable) {
          const filterValue = filterValues[k] || {}
          const columnType = columnTypes[index]
          const isNumber = ['INT', 'BIGINT', 'DOUBLE']
          const isDatetime = ['DATE', 'DATETIME']

          if (isNumber.indexOf(columnType) >= 0) {
            filterDropdown = (
              <NumberFilterDropdown
                from={filterValue.from}
                to={filterValue.to}
                onFromChange={this.onNumberInputChange(k, 'from')}
                onToChange={this.onNumberInputChange(k, 'to')}
                onSearch={this.onLoadData}
              />
            )
          } else if (isDatetime.indexOf(columnType) >= 0) {
            filterDropdown = (
              <DateFilterDropdown
                from={filterValue.from}
                to={filterValue.to}
                onChange={this.onRangePickerChange(k)}
              />
            )
          } else {
            filterDropdown = (
              <SearchFilterDropdown
                columnName={k}
                filterValue={filterValues[k] === undefined ? '' : filterValues[k].from}
                onSearchInputChange={this.onSearchInputChange(k)}
                onSearch={this.onLoadData}
              />
            )
          }

          filters = {
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
        }

        let sorters = null

        if (sortable) {
          sorters = {
            sorter: true,
            sortOrder: sortedInfo.columnKey === k && sortedInfo.order
          }
        }

        const dimensionClass = classnames({
          [styles.dimension]: dimensionColumns && dimensionColumns.indexOf(k) === dimensionColumns.length - 1
        })

        let plainColumn = {
          title: k.toUpperCase(),
          dataIndex: k,
          key: k,
          width: COLUMN_WIDTH,
          className: dimensionClass
        }

        return Object.assign(plainColumn, filters, sorters)
      })

    const predictColumnsWidth = columnKeys.length * COLUMN_WIDTH
    const tableWidthObj = predictColumnsWidth > width
      ? { x: predictColumnsWidth }
      : null
    const tableSize = Object.assign({}, tableWidthObj, { y: height })

    return (
      <Table
        className={className}
        rowKey="antDesignTableId"
        dataSource={dataSource}
        columns={columns}
        pagination={pagination}
        loading={loading}
        scroll={tableSize}
        onChange={this.handleTableChange}
        bordered
      />
    )
  }
}

TableChart.propTypes = {
  data: PropTypes.object,
  loading: PropTypes.bool,
  dimensionColumns: PropTypes.array,
  metricColumns: PropTypes.array,
  className: PropTypes.string,
  filterable: PropTypes.bool,
  sortable: PropTypes.bool,
  width: PropTypes.number,
  height: PropTypes.number,
  onChange: PropTypes.func
}

export default TableChart
