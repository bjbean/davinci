import { env } from '../globalConfig'

export function readListAdapter (data) {
  switch (env) {
    case 'production':
      return data.payload
    default:
      return data
  }
}

export function readObjectAdapter (data) {
  switch (env) {
    case 'production':
      return data.payload[0]
    default:
      return data
  }
}

export function writeAdapter (data) {
  switch (env) {
    case 'production':
      return {
        payload: [data]
      }
    default:
      return data
  }
}
