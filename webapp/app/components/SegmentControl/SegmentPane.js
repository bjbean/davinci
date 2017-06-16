import React, { PropTypes } from 'react'
import classnames from 'classnames'

import utilStyles from '../../assets/less/util.less'
import styles from './SegmentControl.less'

export class SegmentPane extends React.Component {
  render () {
    const {
      active,
      children
    } = this.props

    const panelClass = classnames({
      [utilStyles.hide]: !active
    })

    return (
      <div className={`${styles.segmentPane} ${panelClass}`}>
        {children}
      </div>
    )
  }
}

SegmentPane.propTypes = {
  active: PropTypes.bool,
  children: PropTypes.any
}

export default SegmentPane
