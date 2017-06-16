import React, { PropTypes } from 'react'

import Form from 'antd/lib/form'
import Input from 'antd/lib/input'
import InputNumber from 'antd/lib/input-number'
import Select from 'antd/lib/select'
import Checkbox from 'antd/lib/checkbox'
// import Icon from 'antd/lib/icon'
import Row from 'antd/lib/row'
import Col from 'antd/lib/col'
const FormItem = Form.Item
const Option = Select.Option
const CheckboxGroup = Checkbox.Group

import chartIconMapping from './chartIconMapping'

import utilStyles from '../../assets/less/util.less'
import styles from './Widget.less'

export class WidgetForm extends React.Component {
  render () {
    const {
      form,
      bizlogics,
      widgetlibs,
      dataSource,
      chartInfo,
      onBizlogicChange,
      onWidgetTypeChange,
      onFormItemChange
    } = this.props
    const { getFieldDecorator } = form

    const bizlogicOptions = bizlogics.map(b => (
      <Option key={b.id} value={`${b.id}`}>{b.name}</Option>
    ))

    const widgetlibOptions = widgetlibs.map(w => (
      <Option key={w.id} value={`${w.id}`}>
        {w.name}
        {
          `${w.id}` !== form.getFieldValue('widgetlib_id')
            ? (
              <i className={`iconfont ${chartIconMapping[w.type]} ${styles.chartSelectOption}`} />
            ) : ''
        }
      </Option>
    ))

    let chartParams = ''

    if (chartInfo) {
      const columns = dataSource && dataSource.length ? Object.keys(dataSource[0]) : []
      const parsedParams = JSON.parse(chartInfo.params)

      chartParams = parsedParams.map(info => {
        const formItems = info.items.map(item => {
          let formItem = ''

          switch (item.component) {
            case 'select':
              formItem = (
                <Col key={item.name} span={24}>
                  <FormItem label={item.title}>
                    {getFieldDecorator(item.name, {})(
                      <Select
                        placeholder={item.tip || item.name}
                        onChange={onFormItemChange(item.name)}
                      >
                        {
                          columns.map(c => (
                            <Option key={c} value={c}>{c}</Option>
                          ))
                        }
                      </Select>
                    )}
                  </FormItem>
                </Col>
              )
              break
            case 'multiSelect':
              formItem = (
                <Col key={item.name} span={24}>
                  <FormItem label={item.title}>
                    {getFieldDecorator(item.name, {})(
                      <Select
                        placeholder={item.tip || item.name}
                        mode="multiple"
                        onChange={onFormItemChange(item.name)}
                      >
                        {
                          columns.map(c => (<Option key={c} value={c}>{c}</Option>))
                        }
                      </Select>
                    )}
                  </FormItem>
                </Col>
              )
              break
            case 'checkbox':
              formItem = (
                <Col key={item.name} span={12}>
                  <FormItem label="">
                    {getFieldDecorator(item.name, {
                      initialValue: []
                    })(
                      <CheckboxGroup
                        options={[{label: item.title, value: item.name}]}
                        onChange={onFormItemChange(item.name)}
                      />
                    )}
                  </FormItem>
                </Col>
              )
              break
            case 'inputnumber':
              formItem = (
                <Col key={item.name} span={12}>
                  <FormItem label={item.title}>
                    {getFieldDecorator(item.name, {
                      initialValue: 0
                    })(
                      <InputNumber
                        placeholder={item.tip || item.name}
                        min={item.min || 0}
                        max={item.max || 1000000000000}
                        onChange={onFormItemChange(item.name)}
                      />
                    )}
                  </FormItem>
                </Col>
              )
              break
            default:
              break
          }

          return formItem
        })

        return (
          <div className={styles.chartParams} key={info.name}>
            <h4 className={styles.paramsTitle}>{info.title}</h4>
            <Row className={styles.paramsRegion}>
              {formItems}
            </Row>
          </div>
        )
      })
    }

    return (
      <Form className={styles.formView}>
        <Row>
          <Col span={24}>
            <FormItem className={utilStyles.hide}>
              {getFieldDecorator('id', {})(
                <Input />
              )}
            </FormItem>
            <FormItem label="Widget 名称">
              {getFieldDecorator('name', {
                rules: [{ required: true }]
              })(
                <Input placeholder="Widget Name" />
              )}
            </FormItem>
          </Col>
          <Col span={24}>
            <FormItem label="Widget 描述">
              {getFieldDecorator('desc', {
                initialValue: ''
              })(
                <Input placeholder="Widget Description" />
              )}
            </FormItem>
          </Col>
          <Col lg={24}>
            <FormItem label="Bizlogic">
              {getFieldDecorator('flatTable_id', {
                rules: [{ required: true }]
              })(
                <Select
                  placeholder="请选择 Bizlogic"
                  onChange={onBizlogicChange}
                >
                  {bizlogicOptions}
                </Select>
              )}
            </FormItem>
          </Col>
          <Col span={24}>
            <FormItem label="Widget 类型">
              {getFieldDecorator('widgetlib_id', {
                rules: [{ required: true }]
              })(
                <Select
                  placeholder="Widget Type"
                  onChange={onWidgetTypeChange}
                >
                  {widgetlibOptions}
                </Select>
              )}
            </FormItem>
          </Col>
        </Row>
        {chartParams}
      </Form>
    )
  }
}

WidgetForm.propTypes = {
  form: PropTypes.any,
  bizlogics: PropTypes.array,
  widgetlibs: PropTypes.array,
  dataSource: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.array
  ]),
  chartInfo: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.object
  ]),
  onBizlogicChange: PropTypes.func,
  onWidgetTypeChange: PropTypes.func,
  onFormItemChange: PropTypes.func
}

export default Form.create({withRef: true})(WidgetForm)
