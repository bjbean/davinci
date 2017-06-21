import React, { PropTypes } from 'react'

import Input from 'antd/lib/input'
import Select from 'antd/lib/select'
import DatePicker from 'antd/lib/date-picker'
const InputGroup = Input.Group
const RangePicker = DatePicker.RangePicker

import utilStyles from '../../assets/less/util.less'

export function NumberFilterDropdown (props) {
  return (
    <div className={utilStyles.searchFilterDropdown}>
      <InputGroup size="large" compact>
        <Select size="large" defaultValue="1">
          <Option value="1">之间</Option>
          <Option value="2">之外</Option>
        </Select>
        <RangePicker
          format={'YYYY-MM-DD'}
          onChange={props.onChange}
          onOk={props.onSearch}
        />
      </InputGroup>
    </div>
  )
}

NumberFilterDropdown.propTypes = {
  // from: PropTypes.string,
  // to: PropTypes.string,
  onChange: PropTypes.func,
  onSearch: PropTypes.func
}

export default NumberFilterDropdown
