import React, { Component, PropTypes } from 'react'
import classnames from 'classnames'

import styles from '../Dashboard.less'

export class DashboardItemControlPanel extends Component {
  render () {
    const panelClass = classnames({
      [styles.controlPanel]: true,
      [styles.show]: this.props.show
    })

    const formClass = classnames({
      [styles.form]: true,
      [styles.show]: this.props.show
    })

    return (
      <div className={panelClass}>
        <div className={formClass}>
          {this.props.children}
        </div>
      </div>
    )
  }
}

DashboardItemControlPanel.propTypes = {
  show: PropTypes.bool,
  children: PropTypes.node
}

export default DashboardItemControlPanel
