import React, { PropTypes } from 'react'

import Form from 'antd/lib/form'
import Row from 'antd/lib/row'
import Col from 'antd/lib/col'
import Input from 'antd/lib/input'
import Select from 'antd/lib/select'
const FormItem = Form.Item
const Option = Select.Option

import utilStyles from '../../assets/less/util.less'

export class SourceForm extends React.PureComponent {

  render () {
    const { getFieldDecorator } = this.props.form

    const commonFormItemStyle = {
      labelCol: { span: 6 },
      wrapperCol: { span: 16 }
    }

    return (
      <Form>
        <Row gutter={8}>
          <Col span={24}>
            <FormItem className={utilStyles.hide}>
              {getFieldDecorator('id', {
                hidden: this.props.type === 'add'
              })(
                <Input />
              )}
            </FormItem>
            <FormItem label="名称" {...commonFormItemStyle}>
              {getFieldDecorator('name', {
                rules: [{
                  required: true,
                  message: 'Name 不能为空'
                }]
              })(
                <Input placeholder="Name" />
              )}
            </FormItem>
          </Col>
          <Col span={24}>
            <FormItem label="类型" {...commonFormItemStyle}>
              {getFieldDecorator('type', {
                initialValue: 'moonbox'
              })(
                <Select>
                  <Option value="moonbox">Moonbox</Option>
                  <Option value="jdbc">JDBC</Option>
                </Select>
              )}
            </FormItem>
          </Col>
          <Col span={24}>
            <FormItem label="Url" {...commonFormItemStyle}>
              {getFieldDecorator('connection_url', {
                rules: [{
                  required: true,
                  message: 'Connection Url 不能为空'
                }],
                initialValue: ''
              })(
                <Input placeholder="Connection Url" />
              )}
            </FormItem>
          </Col>
          <Col span={24}>
            <FormItem label="描述" {...commonFormItemStyle}>
              {getFieldDecorator('desc', {
                initialValue: ''
              })(
                <Input
                  placeholder="Description"
                  type="textarea"
                  autosize={{minRows: 2, maxRows: 6}}
                />
              )}
            </FormItem>
          </Col>
          <Col span={24}>
            <FormItem label="配置信息" {...commonFormItemStyle}>
              {getFieldDecorator('config', {
                initialValue: ''
              })(
                <Input
                  placeholder="Config"
                  type="textarea"
                  autosize={{minRows: 2, maxRows: 6}}
                />
              )}
            </FormItem>
          </Col>
        </Row>
      </Form>
    )
  }
}

SourceForm.propTypes = {
  type: PropTypes.string,
  form: PropTypes.any
}

export default Form.create({withRef: true})(SourceForm)
