import React, { PropTypes, cloneElement } from 'react'
import classnames from 'classnames'

import SegmentPane from './SegmentPane'

import styles from './SegmentControl.less'

export class SegmentControl extends React.Component {
  static SegmentPane = SegmentPane

  constructor (props) {
    super(props)
    this.state = {
      activeKey: ''
    }
  }

  componentWillMount () {
    const children = React.Children.toArray(this.props.children)
    if (children.length) {
      this.setState({
        activeKey: children[0].key
      })
    }
  }

  itemClick = (key) => () => {
    this.setState({
      activeKey: key
    })
    this.props.onChange(key)
  }

  render () {
    const {
      position
    } = this.props

    const {
      activeKey
    } = this.state

    const children = React.Children.toArray(this.props.children)

    const indicatorPosition = classnames({
      [styles.left]: position === 'left',
      [styles.right]: position === 'right'
    })

    const indicators = []
    const panes = []

    children.forEach(c => {
      let { key } = c
      let { tab, children } = c.props

      let indicatorClass = classnames({
        [styles.active]: key === activeKey
      })

      indicators.push(
        <li
          className={indicatorClass}
          key={key}
          onClick={this.itemClick(key)}
        >
          {tab || ''}
        </li>
      )

      panes.push(cloneElement(c, {
        active: c.key === activeKey,
        children: children
      }))
    })

    return (
      <div className={styles.segmentControl}>
        <ul className={`${styles.indicator} ${indicatorPosition}`}>
          {indicators}
        </ul>
        <div className={styles.segmentPanes}>
          {panes}
        </div>
      </div>
    )
  }
}

SegmentControl.propTypes = {
  // activeKey: PropTypes.string,
  // defaultActiveKey: PropTypes.string,
  position: PropTypes.string,
  onChange: PropTypes.func,
  // onClick: PropTypes.func,
  children: PropTypes.any
}

SegmentControl.defaultProps = {
  position: 'left',
  onChange: () => {}
}

export default SegmentControl
