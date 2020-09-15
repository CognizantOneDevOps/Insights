/*******************************************************************************
 * Copyright 2019 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

import { Injectable } from '@angular/core';
import { DomSanitizer, BrowserModule, SafeUrl } from '@angular/platform-browser';
import { MatIconRegistry } from '@angular/material/icon';


export interface IImageHandlerService {
    getImagePath(imageKey: String): String;
    initializeImageIcons(): void;
    addPathIconRegistry(): void;
}

@Injectable()
export class ImageHandlerService implements IImageHandlerService {
    urlMapping = {};
    imageMap = new Map<String, String>();
    constructor(private iconRegistry: MatIconRegistry, private sanitizer: DomSanitizer) {

    }

    public initializeImageIcons() {
        this.addImage('defaultLogo', "icons/svg/landingPage/OneDevOps_InsightsLOGO.svg");
        this.addImage('InsightsLogo', "icons/svg/login/InsightsLogo.svg");
        this.addImage('CustomerLogo', "icons/svg/login/Customer_Logo.png");
        this.addImage('PoweredBy', "icons/svg/login/PoweredBy.svg");
        this.addImage('verticleLine', "icons/svg/login/Vertical_Line.svg");
        this.addImage('OrangeVerticalLine', "icons/svg/OrangeVerticalLine.svg");
        this.addImage('OrangeLine', "icons/svg/landingPage/OrangeLine.svg");
        this.addImage('user-icon', "icons/svg/login/user_icon.svg");
        this.addImage('user-icon-active', "icons/svg/login/user_icon_active.svg");
        this.addImage('password-icon', "icons/svg/login/password_icon.svg");
        this.addImage('password-icon-active', "icons/svg/login/password_icon_active.svg");
        this.addImage('favicon_icon', "icons/svg/IS.svg");
        this.addImage('healthcheck_success_status', "icons/svg/sharp-check_circle-24px.svg");
        this.addImage('healthcheck_failure_status', "icons/svg/Exclamation.svg");
        this.addImage('healthcheck_show_details', "icons/svg/sharp-list_alt-24px.svg");
        this.addImage('healthcheck_back_to_top', "icons/svg/backToTop.svg");
        this.addImage('close_dialog', "icons/svg/close.svg");
        this.addImage('edit_icon', "icons/svg/actionIcons/Edit_icon_disabled.svg");
        this.addImage('start_icon', "icons/svg/actionIcons/Start_icon_Disabled.svg");
        this.addImage('stop_icon', "icons/svg/actionIcons/Stop_icon_Disabled.svg");
        this.addImage('successIconSrc', "icons/svg/ic_check_circle_24px.svg");
        this.addImage('ic_report_problem', "icons/svg/ic_report_problem_24px.svg");
        this.addImage('ic_delete_icon', "icons/svg/actionIcons/Delete_icon_disabled.svg");
        this.addImage('search_icon', "icons/svg/ic_search_24px.svg");
        this.addImage('addButton', "icons/svg/buttonIcon/Add.svg");
        this.addImage('startButton', "icons/svg/buttonIcon/sharp-play_circle.svg");
        this.addImage('stopButton', "icons/svg/buttonIcon/sharp-stop.svg");
        this.addImage('lineInButton', "icons/svg/buttonIcon/Line_menu.svg");
        this.addImage('warning', "icons/svg/dialogBoxIcon/warning.svg");
        this.addImage('success1', "icons/others/success.png");
        this.addImage('success', "icons/svg/dialogBoxIcon/success.svg");
        this.addImage('error', "icons/svg/dialogBoxIcon/error.svg");
        this.addImage('alert', "icons/svg/confirmBox/alert.svg");
        this.addImage('menuImage', "icons/svg/homePage/menu_white_36.png");
        this.addImage('plus_icon', "icons/svg/auditReporting/Plus.svg");
        this.addImage('minus_icon', "icons/svg/auditReporting/minus.svg");
        this.addImage('active_show_details', "icons/svg/auditReporting/ShowDetails-active.svg");
        this.addImage('inactive_show_details', "icons/svg/auditReporting/ShowDetails-Inactive.svg");
        this.addImage('export_to_pdf_icon', "icons/svg/auditReporting/PDFIcon.svg");
        this.addImage('view_pipe_line', "icons/svg/auditReporting/ViewPipeLine-GREEN.svg");
        this.addImage('ic_about_logo', "icons/svg/ic_about_logo.svg");
        this.addImage('ic_Insights_default_logo', "icons/svg/landingPage/Insights_Logo.png");
        this.addImage('redirect_icon', "icons/svg/userOnboarding/sharp-supervised_user_circle-24px.svg");
        this.addImage('unsubscribe_webhook', "icons/svg/webhook/WebhookUnsubscribe.svg");
        this.addImage('unsubscribe_webhookDeactive', "icons/svg/webhook/WebhookUnsubscribe-Deactivated.svg");
        this.addImage('webhook', "icons/svg/webhook/WebHook.svg");
        this.addImage('webhookDeactive', "icons/svg/webhook/WebHookDetactivate.svg");
        this.addImage('webhookCopyClipboard', "icons/svg/webhook/CopyClipboard.svg");
        this.addImage('dashboard', "icons/svg/dashboard.svg");
        this.addImage('view', "icons/svg/view.svg");
        this.addImage('homeButton', "icons/svg/home.svg")
    }


    public addPathIconRegistry() {
        this.imageMap.forEach((value: string, key: string) => {
            this.iconRegistry.addSvgIcon(key, this.sanitizer.bypassSecurityTrustResourceUrl(value));
        });
    }

    public addImage(name: string, imagePath: String) {
        if (!this.imageMap.has(name)) {
            this.imageMap.set(name, imagePath);
        } /* else {
            throw new Error('imagePath with same name already exists ' + name);
        } */
    }
    public getImagePath(imageKey: String) {
        if (!this.imageMap.has(imageKey)) {
            throw new Error("Url Mapping doesnt exist");
        }
        return this.imageMap.get(imageKey);
    }
}