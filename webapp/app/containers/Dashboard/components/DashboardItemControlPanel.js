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

        </div>
      </div>
    )
  }
}

DashboardItemControlPanel.propTypes = {
  show: PropTypes.bool
}

export default DashboardItemControlPanel
