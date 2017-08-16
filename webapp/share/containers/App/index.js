import React, { PropTypes } from 'react'
import Helmet from 'react-helmet'

export function App (props) {
  return (
    <div>
      <Helmet
        titleTemplate="%s - Davinci"
        defaultTitle="Davinci Web Application"
        meta={[
          { name: 'description', content: 'Davinci web application built for data visualization' }
        ]}
      />
      {React.Children.toArray(props.children)}
    </div>
  )
}

App.propTypes = {
  children: PropTypes.node
}

export default App
