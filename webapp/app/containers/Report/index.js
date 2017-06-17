import React, { PropTypes } from 'react'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import classnames from 'classnames'

import Sidebar from '../../components/Sidebar'
import SidebarOption from '../../components/SidebarOption'
import { selectSidebar } from './selectors'
import { loadSidebar } from './actions'
import { makeSelectLoginUser } from '../App/selectors'
import styles from './Report.less'

export class Report extends React.Component {

  componentDidMount () {
    this.props.onPageLoad()
  }

  render () {
    const {
      sidebar,
      loginUser,
      routes
    } = this.props

    const sidebarOptions = sidebar && sidebar.map(item => {
      const isOptionActive = item.route.indexOf(routes[3].name) >= 0
      const iconClassName = `iconfont ${item.icon}`
      return (
        <SidebarOption
          key={item.route}
          route={item.route}
          active={isOptionActive}>
          <i className={iconClassName}></i>
        </SidebarOption>
      )
    })

    const sidebarComponent = loginUser.admin
      ? (
        <Sidebar>
          {sidebarOptions}
        </Sidebar>
      ) : ''

    const mainClass = classnames({
      [styles.main]: true,
      [styles.admin]: loginUser.admin
    })

    return (
      <div className={styles.report}>
        {sidebarComponent}
        <div className={mainClass}>
          {this.props.children}
        </div>
      </div>
    )
  }
}

Report.propTypes = {
  sidebar: PropTypes.oneOfType([
    PropTypes.array,
    PropTypes.bool
  ]),
  loginUser: PropTypes.object,
  onPageLoad: PropTypes.func,
  routes: PropTypes.array,
  children: PropTypes.node
}

const mapStateToProps = createStructuredSelector({
  sidebar: selectSidebar(),
  loginUser: makeSelectLoginUser()
})

export function mapDispatchToProps (dispatch) {
  return {
    onPageLoad: () => {
      const sidebarSource = [
        { icon: 'icon-dashboard', route: ['dashboard', 'grid'] },
        { icon: 'icon-widget-gallery', route: ['widget'] },
        { icon: 'icon-custom-business', route: ['bizlogic'] },
        { icon: 'icon-datasource24', route: ['source'] },
        { icon: 'icon-user1', route: ['user'] },
        { icon: 'icon-group', route: ['group'] }
      ]
      dispatch(loadSidebar(sidebarSource))
    }
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Report)
