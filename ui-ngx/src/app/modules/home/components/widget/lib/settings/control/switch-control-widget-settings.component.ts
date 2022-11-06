import { Component } from '@angular/core';
import { WidgetSettings, WidgetSettingsComponent } from '@shared/models/widget.models';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Store } from '@ngrx/store';
import { AppState } from '@core/core.state';
import { switchRpcDefaultSettings } from '@home/components/widget/lib/settings/control/switch-rpc-settings.component';
import { deepClone } from '@core/utils';

@Component({
  selector: 'tb-switch-control-widget-settings',
  templateUrl: './switch-control-widget-settings.component.html',
  styleUrls: ['./../widget-settings.scss']
})
export class SwitchControlWidgetSettingsComponent extends WidgetSettingsComponent {

  switchControlWidgetSettingsForm: FormGroup;

  constructor(protected store: Store<AppState>,
              private fb: FormBuilder) {
    super(store);
  }

  get targetDeviceAliasId(): string {
    const aliasIds = this.widget?.config?.targetDeviceAliasIds;
    if (aliasIds && aliasIds.length) {
      return aliasIds[0];
    }
    return null;
  }

  protected settingsForm(): FormGroup {
    return this.switchControlWidgetSettingsForm;
  }

  protected defaultSettings(): WidgetSettings {
    return {
      title: '',
      showOnOffLabels: true,
      ...switchRpcDefaultSettings()
    };
  }

  protected onSettingsSet(settings: WidgetSettings) {
    this.switchControlWidgetSettingsForm = this.fb.group({
      title: [settings.title, []],
      showOnOffLabels: [settings.showOnOffLabels, []],
      switchRpcSettings: [settings.switchRpcSettings, []]
    });
  }

  protected prepareInputSettings(settings: WidgetSettings): WidgetSettings {
    const switchRpcSettings = deepClone(settings, ['title', 'showOnOffLabels']);
    return {
      title: settings.title,
      showOnOffLabels: settings.showOnOffLabels,
      switchRpcSettings
    };
  }

  protected prepareOutputSettings(settings: any): WidgetSettings {
    return {
      title: settings.title,
      showOnOffLabels: settings.showOnOffLabels,
      ...settings.switchRpcSettings
    };
  }
}
