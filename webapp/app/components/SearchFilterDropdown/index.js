import React, { PropTypes } from 'react'

import Input from 'antd/lib/input'
const Search = Input.Search

import utilStyles from '../../assets/less/util.less'

export function SearchFilterDropdown (props) {
  return (
    <div className={utilStyles.searchFilterDropdown}>
      <Search
        size="large"
        placeholder={`Search ${props.columnName}`}
        value={props.filterValue}
        onChange={props.onSearchInputChange}
        onSearch={props.onSearch}
      />
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
