import React, { PropTypes, PureComponent } from 'react'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import Banner from './Banner3'
import LoginForm from './LoginForm'
import Row from 'antd/lib/row'
import Col from 'antd/lib/col'
import Button from 'antd/lib/button'

import { login } from '../App/actions'
import { makeSelectLogged } from '../App/selectors'
import { promiseDispatcher } from '../../utils/reduxPromisation'

import styles from './Login3.less'

export class Login extends PureComponent {
  componentWillMount () {
    const {
      logged,
      router
    } = this.props
    if (logged) router.push('/')
  }

  doLogin = () => {
    const {
      onLogin,
      router
    } = this.props
    this.loginForm.validateFieldsAndScroll((err, { username, password }) => {
      if (!err) {
        onLogin(username, password)
          .then(() => { router.push('/') })
      }
    })
  }

  render () {
    return (
      <div className={styles.login}>
        <div className={styles.logo}>
          <span>D</span>
          <span>a</span>
          <span>v</span>
          <span>i</span>
          <span>n</span>
          <span>c</span>
          <span>i</span>
        </div>
        <Banner />
        <div className={styles.window}>
          <Row gutter={8}>
            <Col sm={21}>
              <LoginForm ref={f => { this.loginForm = f }} />
            </Col>
            <Col sm={3}>
              <Button size="large" onClick={this.doLogin}>登 录</Button>
            </Col>
          </Row>
        </div>
      </div>
    )
  }
}

Login.propTypes = {
  logged: PropTypes.bool,
  router: PropTypes.any,
  onLogin: PropTypes.func
}

const mapStateToProps = createStructuredSelector({
  logged: makeSelectLogged()
})

export function mapDispatchToProps (dispatch) {
  return {
    onLogin: (username, password) => promiseDispatcher(dispatch, login, username, password)
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Login)

