import React, { PropTypes } from 'react'

import styles from './Sidebar.less'

export default class Sidebar extends React.Component {
  render () {
    return (
      <div className={styles.sidebar}>
        {this.props.children}
      </div>
    )
  }
}

Sidebar.propTypes = {
  children: PropTypes.node
}

