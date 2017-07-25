import React, { Component, PropTypes } from 'react'

export class Share extends Component {
  render () {
    return (
      <div>
        {this.props.children}
      </div>
    )
  }
}

Share.propTypes = {
  children: PropTypes.node
}

export default Share
