import React, { PropTypes } from 'react'

import Input from 'antd/lib/input'
import Button from 'antd/lib/button'

import utilStyles from '../../assets/less/util.less'

export function SearchFilterDropdown (props) {
  return (
    <div className={utilStyles.searchFilterDropdown}>
      <Input
        size="large"
        placeholder={`Search${props.columnName}`}
        value={props.filterValue}
        onChange={props.onSearchInputChange}
        onPressEnter={props.onSearch}
      />
      <Button type="primary" onClick={props.onSearch}>Search</Button>
    </div>
  )
}

SearchFilterDropdown.propTypes = {
  columnName: PropTypes.string,
  filterValue: PropTypes.string,
  onSearchInputChange: PropTypes.func,
  onSearch: PropTypes.func
}

export default SearchFilterDropdown
