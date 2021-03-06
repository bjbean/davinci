import React, { PropTypes } from 'react'

import Form from 'antd/lib/form'
import Row from 'antd/lib/row'
import Col from 'antd/lib/col'
import Input from 'antd/lib/input'
const FormItem = Form.Item

import utilStyles from '../../assets/less/util.less'

export class UserPasswordForm extends React.PureComponent {
  checkPasswordConfirm = (rule, value, callback) => {
    if (value && value !== this.props.form.getFieldValue('newPass')) {
      callback('两次输入的密码不一致')
    } else {
      callback()
    }
  }

  forceCheckConfirm = (rule, value, callback) => {
    const { form } = this.props
    if (form.getFieldValue('confirmPassword')) {
      form.validateFields(['confirmPassword'], { force: true })
    }
    callback()
  }

  render () {
    const { getFieldDecorator } = this.props.form

    const commonFormItemStyle = {
      labelCol: { span: 8 },
      wrapperCol: { span: 14 }
    }

    return (
      <Form>
        <Row gutter={8}>
          <Col span={24}>
            <FormItem className={utilStyles.hide}>
              {getFieldDecorator('id', {})(
                <Input />
              )}
            </FormItem>
            <FormItem label="旧密码" {...commonFormItemStyle}>
              {getFieldDecorator('oldPass', {
                rules: [{
                  required: true,
                  message: '旧密码不能为空'
                }, {
                  min: 6,
                  max: 20,
                  message: '密码长度为6-20位'
                }]
              })(
                <Input type="password" placeholder="Your Password" />
              )}
            </FormItem>
          </Col>
          <Col span={24}>
            <FormItem label="新密码" {...commonFormItemStyle}>
              {getFieldDecorator('newPass', {
                rules: [{
                  required: true,
                  message: '新密码不能为空'
                }, {
                  min: 6,
                  max: 20,
                  message: '密码长度为6-20位'
                }, {
                  validator: this.forceCheckConfirm
                }]
              })(
                <Input type="password" placeholder="New Password" />
              )}
            </FormItem>
          </Col>
          <Col span={24}>
            <FormItem label="确认新密码" {...commonFormItemStyle}>
              {getFieldDecorator('confirmPassword', {
                rules: [{
                  required: true,
                  message: '请确认密码'
                }, {
                  validator: this.checkPasswordConfirm
                }]
              })(
                <Input type="password" placeholder="Confirm Password" />
              )}
            </FormItem>
          </Col>
        </Row>
      </Form>
    )
  }
}

UserPasswordForm.propTypes = {
  form: PropTypes.any
}

export default Form.create({withRef: true})(UserPasswordForm)
