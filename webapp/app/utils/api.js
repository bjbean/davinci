import config, { env } from '../globalConfig'

const host = config[env].host

export default {
  login: `${host}/login`,
  group: `${host}/groups`,
  user: `${host}/users`,
  changepwd: `${host}/changepwd`,
  source: `${host}/sources`,
  bizlogic: `${host}/flattables`,
  widgetlib: `${host}/libwidgets`,
  // bizdata: `${host}/bizdatas`,
  widget: `${host}/widgets`,
  dashboard: `${host}/dashboards`
}
