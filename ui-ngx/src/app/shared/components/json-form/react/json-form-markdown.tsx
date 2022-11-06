import * as React from 'react';
import ThingsboardAceEditor from './json-form-ace-editor';
import { JsonFormFieldProps, JsonFormFieldState } from '@shared/components/json-form/react/json-form.models';

class ThingsboardMarkdown extends React.Component<JsonFormFieldProps, JsonFormFieldState> {

  constructor(props) {
    super(props);
  }

  render() {
    return (
      <ThingsboardAceEditor {...this.props} mode='markdown' {...this.state}></ThingsboardAceEditor>
    );
  }
}

export default ThingsboardMarkdown;
