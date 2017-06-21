import React, { PropTypes } from 'react'

import Input from 'antd/lib/input'
import Select from 'antd/lib/select'
const InputGroup = Input.Group

import utilStyles from '../../assets/less/util.less'

export function NumberFilterDropdown (props) {
  return (
    <div className={utilStyles.searchFilterDropdown}>
      <InputGroup size="large" compact>
        <Select size="large" defaultValue="1">
          <Option value="1">之间</Option>
          <Option value="2">之外</Option>
        </Select>
        <Input
          className={utilStyles.number}
          value={props.from}
          placeholder="从"
          onChange={props.onFromChange}
          onPressEnter={props.onSearch}
        />
        <Input className={utilStyles.numberDivider} placeholder="~" readOnly tabIndex="-1" />
        <Input
          className={`${utilStyles.number} ${utilStyles.to}`}
          value={props.to}
          placeholder="到"
          onChange={props.onToChange}
          onPressEnter={props.onSearch}
        />
      </InputGroup>
    </div>
  )
}

NumberFilterDropdown.propTypes = {
  from: PropTypes.string,
  to: PropTypes.string,
  onFromChange: PropTypes.func,
  onToChange: PropTypes.func,
  onSearch: PropTypes.func
}

export default NumberFilterDropdown
