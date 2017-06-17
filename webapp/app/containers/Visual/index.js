import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import Navigator from '../../components/Navigator'

import { logged, logout, setLoginUser, getLoginUser } from '../App/actions'
import { makeSelectLogged } from '../App/selectors'
import { promiseDispatcher } from '../../utils/reduxPromisation'
import checkLogin from '../../utils/checkLogin'
import { setToken } from '../../utils/request'

export class Visual extends Component {
  componentWillMount () {
    this.checkTokenLink()
  }

  checkTokenLink = () => {
    const {
      router,
      onGetLoginUser
    } = this.props

    const qs = this.getQs()
    const token = qs['token']
    const dashboard = qs['dashboard']

    if (token) {
      setToken(token)
      localStorage.setItem('TOKEN', token)
      localStorage.setItem('TOKEN_EXPIRE', new Date().getTime() + 3600000)
      onGetLoginUser()
        .then((user) => {
          if (dashboard) {
            router.push(`/visual/report/grid/${dashboard}`)
          } else {
            router.push('/visual')
          }
        })
    } else {
      this.checkNormalLogin()
    }
  }

  getQs = () => {
    const search = location.search
    const qs = search ? search.substr(1) : ''
    if (qs) {
      return qs
        .split('&')
        .reduce((rdc, val) => {
          const pair = val.split('=')
          rdc[pair[0]] = pair[1]
          return rdc
        }, {})
    } else {
      return false
    }
  }

  checkNormalLogin = () => {
    if (checkLogin()) {
      const token = localStorage.getItem('TOKEN')
      const loginUser = localStorage.getItem('loginUser')

      setToken(token)
      this.props.onLogged()
      this.props.onSetLoginUser(JSON.parse(loginUser))
    } else {
      this.props.router.push('/login')
    }
  }

  logout = () => {
    const {
      router,
      onLogout
    } = this.props
    onLogout()
    localStorage.removeItem('TOKEN')
    localStorage.removeItem('TOKEN_EXPIRE')
    router.push('/login')
  }

  render () {
    const { logged, children } = this.props
    const content = logged
      ? (
        <div>
          <Navigator onLogout={this.logout} />
          {children}
        </div>
      )
      : (
        <div></div>
      )

    return content
  }
}

Visual.propTypes = {
  children: PropTypes.node,
  router: PropTypes.any,
  logged: PropTypes.bool,
  onLogged: PropTypes.func,
  onLogout: PropTypes.func,
  onSetLoginUser: PropTypes.func,
  onGetLoginUser: PropTypes.func
}

const mapStateToProps = createStructuredSelector({
  logged: makeSelectLogged()
})

export function mapDispatchToProps (dispatch) {
  return {
    onLogged: () => promiseDispatcher(dispatch, logged),
    onLogout: () => promiseDispatcher(dispatch, logout),
    onSetLoginUser: (user) => promiseDispatcher(dispatch, setLoginUser, user),
    onGetLoginUser: () => promiseDispatcher(dispatch, getLoginUser)
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Visual)
