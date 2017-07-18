import React, { PropTypes, PureComponent } from 'react'

import Form from 'antd/lib/form'
import Row from 'antd/lib/row'
import Col from 'antd/lib/col'
import Input from 'antd/lib/input'
const FormItem = Form.Item

import styles from './Login3.less'

export class LoginForm extends PureComponent {
  render () {
    const { getFieldDecorator } = this.props.form

    return (
      <Form>
        <Row gutter={8}>
          <Col sm={12}>
            <FormItem className={styles.loginFormItem}>
              {getFieldDecorator('username', {
                rules: [{
                  required: true,
                  message: '请输入用户名'
                }],
                initialValue: 'test@creditease.cn'
              })(
                <Input placeholder="用户名" />
              )}
            </FormItem>
          </Col>
          <Col sm={12}>
            <FormItem className={styles.loginFormItem}>
              {getFieldDecorator('password', {
                rules: [{
                  required: true,
                  message: '请输入密码'
                }],
                initialValue: '123456'
              })(
                <Input placeholder="密码" type="password" />
              )}
            </FormItem>
          </Col>
        </Row>
      </Form>
    )
  }
}

LoginForm.propTypes = {
  form: PropTypes.any
}

export default Form.create({withRef: true})(LoginForm)
