import React, { PropTypes } from 'react'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import Icon from 'antd/lib/icon'

import { makeSelectLoginUser } from '../../containers/App/selectors'

import styles from './Navigator.less'

export function Navigator (props) {
  return (
    <nav className={styles.header}>
      <div className={styles.logo}>
        <span>D</span>
        <span>a</span>
        <span>v</span>
        <span>i</span>
        <span>n</span>
        <span>c</span>
        <span>i</span>
      </div>
      <ul className={styles.tools}>
        <li>
          <p>{props.loginUser.email}</p>
        </li>
        <li>
          <Icon type="logout" onClick={props.onLogout} />
        </li>
      </ul>
    </nav>
  )
}

Navigator.propTypes = {
  loginUser: PropTypes.object,
  onLogout: PropTypes.func
}

const mapStateToProps = createStructuredSelector({
  loginUser: makeSelectLoginUser()
})

export default connect(mapStateToProps, null)(Navigator)
