import axios from 'axios'
import message from 'antd/lib/message'
// import { notifyError } from './util'

axios.defaults.validateStatus = function (status) {
  return status < 500
}

function parseJSON (response) {
  return response.data
}

function refreshToken (response) {
  const token = response.data.header.token
  if (token) {
    setToken(token)
    localStorage.setItem('TOKEN', token)
    localStorage.setItem('TOKEN_EXPIRE', new Date().getTime() + 3600000)
  }
  return response
}

function checkStatus (response) {
  switch (response.status) {
    case 401:
      message.error('未登录或会话过期，请重新登录', 5)
      removeToken()
      localStorage.removeItem('token')
      break
    default:
      break
  }
  return response
}

export default function request (url, options) {
  return axios(url, options)
    .then(checkStatus)
    .then(refreshToken)
    .then(parseJSON)
}

export function setToken (token) {
  axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
}

export function removeToken () {
  delete axios.defaults.headers.common['Authorization']
}
