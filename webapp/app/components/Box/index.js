import React, { PropTypes } from 'react'

import styles from './Box.less'

export class Box extends React.Component {

  static Header = (props) => (
    <div className={styles.header}>
      {props.children}
    </div>
  )

  static Title = (props) => (
    <h3 className={styles.title}>
      {props.children}
    </h3>
  )

  static Tools = (props) => (
    <div className={styles.tools}>
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
      <div className={styles.box}>
        {this.props.children}
      </div>
    )
  }
}

Box.propTypes = {
  children: PropTypes.node
}

export default Box
