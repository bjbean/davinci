import React, { PropTypes } from 'react'

import styles from './Container.less'

export class Container extends React.Component {

  static Title = (props) => (
    <div className={styles.title}>
      {props.children}
    </div>
  )

  static Body = (props) => (
    <div className={styles.body}>
      {props.children}
    </div>
  )

  render () {
    return (
      <div className={styles.container}>
        {this.props.children}
      </div>
    )
  }
}

Container.propTypes = {
  children: PropTypes.node
}

export default Container
