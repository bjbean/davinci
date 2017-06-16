import React, { PropTypes } from 'react'

import Row from 'antd/lib/row'
import Col from 'antd/lib/col'
import Transfer from 'antd/lib/transfer'

export function GroupTransfer (props) {
  return (
    <Row>
      <Col span={24}>
        <Transfer
          titles={['列表', '已选']}
          listStyle={{width: '220px'}}
          dataSource={props.source}
          rowKey={s => s.id}
          targetKeys={props.target}
          render={item => item.name}
          onChange={(nextTargetKeys, direction, moveKeys) => {
            props.onChange(nextTargetKeys)
          }}
        />
      </Col>
    </Row>
  )
}

GroupTransfer.propTypes = {
  source: PropTypes.array.isRequired,
  target: PropTypes.array.isRequired,
  onChange: PropTypes.func.isRequired
}

export default GroupTransfer
