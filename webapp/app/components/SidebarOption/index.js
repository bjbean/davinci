import React, { PropTypes } from 'react'
import classnames from 'classnames'
import { Link } from 'react-router'

import styles from '../Sidebar/Sidebar.less'

export class SidebarOption extends React.PureComponent {
  render () {
    const optionClass = classnames(
      { [styles.option]: true },
      { [styles.active]: this.props.active }
    )

    const linkRoute = `/visual/report/${this.props.route[0]}`

    return (
      <div className={optionClass}>
        <Link to={linkRoute}>
          {this.props.children}
        </Link>
      </div>
    )
  }
}

SidebarOption.propTypes = {
  route: PropTypes.array,
  active: PropTypes.bool,
  children: PropTypes.node
}

export default SidebarOption
