import React, { PropTypes, PureComponent } from 'react'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import Helmet from 'react-helmet'

import Navigator from '../../components/Navigator'

import { logged, logout, setLoginUser } from './actions'
import { makeSelectLogged } from './selectors'
import { promiseDispatcher } from '../../utils/reduxPromisation'
import { setToken } from '../../utils/request'

export class App extends PureComponent {
  componentWillMount () {
    this.checkLogin()
  }

  checkLogin = () => {
    const token = localStorage.getItem('TOKEN')

    if (token) {
      const expire = localStorage.getItem('TOKEN_EXPIRE')
      const timestamp = new Date().getTime()

      if (Number(expire) > timestamp) {
        setToken(token)
        this.props.onLogged()

        const loginUser = localStorage.getItem('loginUser')
        this.props.onSetLoginUser(JSON.parse(loginUser))
      } else {
        localStorage.removeItem('TOKEN')
        localStorage.removeItem('TOKEN_EXPIRE')
        this.props.router.push('/login')
      }
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
    const {
      logged,
      children
    } = this.props

    return (
      <div>
        <Helmet
          titleTemplate="%s - Davinci"
          defaultTitle="Davinci Web Application"
          meta={[
            { name: 'description', content: 'Davinci web application built for data visualization' }
          ]}
        />
        {logged ? <Navigator onLogout={this.logout} /> : ''}
        {React.Children.toArray(children)}
      </div>
    )
  }
}

App.propTypes = {
  children: PropTypes.node,
  router: PropTypes.any,
  logged: PropTypes.bool,
  onLogged: PropTypes.func,
  onLogout: PropTypes.func,
  onSetLoginUser: PropTypes.func
}

const mapStateToProps = createStructuredSelector({
  logged: makeSelectLogged()
})

export function mapDispatchToProps (dispatch) {
  return {
    onLogged: () => promiseDispatcher(dispatch, logged),
    onLogout: () => promiseDispatcher(dispatch, logout),
    onSetLoginUser: (user) => promiseDispatcher(dispatch, setLoginUser, user)
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(App)
