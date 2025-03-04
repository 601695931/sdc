/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.ci.tests.datatypes;

public class AmdocsLicenseMembers {

	private String vendorId;
	private String vendorLicenseName;
	private String vendorLicenseAgreementId;
	private String featureGroupId;
	private String licenseVersionId;
	private String licenseVersionLabel;
	private String version;

	public AmdocsLicenseMembers(String vendorId, String vendorLicenseName, String vendorLicenseAgreementId,
			String featureGroupId, String licenseVersionId, String licenseVersionLabel) {
		this.vendorId = vendorId;
		this.vendorLicenseName = vendorLicenseName;
		this.vendorLicenseAgreementId = vendorLicenseAgreementId;
		this.featureGroupId = featureGroupId;
		this.licenseVersionId = licenseVersionId;
		this.licenseVersionLabel = licenseVersionLabel;
	}

	public String getLicenseVersionId() {
		return licenseVersionId;
	}

	public void setLicenseVersionId(String licenseVersionId) {
		this.licenseVersionId = licenseVersionId;
	}

	public String getLicenseVersionLabel() {
		return licenseVersionLabel;
	}

	public void setLicenseVersionLabel(String licenseVersionLabel) {
		this.licenseVersionLabel = licenseVersionLabel;
	}

	public AmdocsLicenseMembers(String vendorId, String vendorLicenseName, String vendorLicenseAgreementId, String featureGroupId) {
		super();
		this.vendorId = vendorId;
		this.vendorLicenseName = vendorLicenseName;
		this.vendorLicenseAgreementId = vendorLicenseAgreementId;
		this.featureGroupId = featureGroupId;
	}

	public String getVendorId() {
		return vendorId;
	}

	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}

	public String getVendorLicenseName() {
		return vendorLicenseName;
	}

	public void setVendorLicenseName(String vendorLicenseName) {
		this.vendorLicenseName = vendorLicenseName;
	}

	public String getVendorLicenseAgreementId() {
		return vendorLicenseAgreementId;
	}

	public void setVendorLicenseAgreementId(String vendorLicenseAgreementId) {
		this.vendorLicenseAgreementId = vendorLicenseAgreementId;
	}

	public String getFeatureGroupId() {
		return featureGroupId;
	}

	public void setFeatureGroupId(String featureGroupId) {
		this.featureGroupId = featureGroupId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "AmdocsLicenseMembers{" +
				"vendorId='" + vendorId + '\'' +
				", vendorLicenseName='" + vendorLicenseName + '\'' +
				", vendorLicenseAgreementId='" + vendorLicenseAgreementId + '\'' +
				", featureGroupId='" + featureGroupId + '\'' +
				", licenseVersionId='" + licenseVersionId + '\'' +
				", licenseVersionLabel='" + licenseVersionLabel + '\'' +
				", version='" + version + '\'' +
				'}';
	}


}
