/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.stream.Collectors;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToVSPGeneralConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToVSPQuestionnaireConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.versioning.ActionVersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityStoreType;

public class VendorSoftwareProductInfoDaoZusammenImpl implements VendorSoftwareProductInfoDao {
  private static final String EMPTY_DATA = "{}";

  private ZusammenAdaptor zusammenAdaptor;

  public VendorSoftwareProductInfoDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersionableEntityMetadata metadata =
        new VersionableEntityMetadata(VersionableEntityStoreType.Zusammen, "VendorSoftwareProduct",
            null, null);

    ActionVersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);
  }

  @Override
  public Collection<VspDetails> list(VspDetails entity) {
    return zusammenAdaptor.listItems(createSessionContext()).stream()
        .filter(item -> "VendorSoftwareProduct".equals(item.getInfo().getProperty("item_type")))
        .map(new ElementToVSPGeneralConvertor()::convert)
        .collect(Collectors.toList());
  }

  @Override
  public void create(VspDetails vspDetails) {
    ZusammenElement generalElement = mapVspDetailsToZusammenElement(vspDetails, Action.CREATE);

    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(vspDetails.getId(), vspDetails.getVersion().getId());
    zusammenAdaptor.saveElement(context, elementContext, generalElement,
        "Create VSP General Info Element");

    createVspStructure(context, elementContext);
  }

  private void createVspStructure(SessionContext context, ElementContext elementContext) {
    createOrchestrationTemplateCandidateStructure(context, elementContext);
    createVspModelStructure(context, elementContext);

    zusammenAdaptor.saveElement(context, elementContext,
        buildStructuralElement(ElementType.DeploymentFlavors, Action.CREATE),
        "Create VSP Deployment Flavors Element");

    zusammenAdaptor.saveElement(context, elementContext,
        buildStructuralElement(ElementType.Processes, Action.CREATE),
        "Create VSP Processes Element");
  }

  private void createOrchestrationTemplateCandidateStructure(SessionContext context,
                                                             ElementContext elementContext) {
    ByteArrayInputStream emptyData = new ByteArrayInputStream(EMPTY_DATA.getBytes());

    ZusammenElement candidateContentElement =
        buildStructuralElement(ElementType.OrchestrationTemplateCandidateContent, Action.CREATE);
    candidateContentElement.setData(emptyData);
    ZusammenElement validationData = buildStructuralElement(ElementType
        .OrchestrationTemplateCandidateValidationData, Action.CREATE);
    ZusammenElement candidateElement =
        buildStructuralElement(ElementType.OrchestrationTemplateCandidate, Action.CREATE);
    candidateElement.setData(emptyData);
    candidateElement.addSubElement(candidateContentElement);
    candidateElement.addSubElement(validationData);
    zusammenAdaptor.saveElement(context, elementContext, candidateElement,
        "Create Orchestration Template Candidate Elements");
  }

  private void createVspModelStructure(SessionContext context, ElementContext elementContext) {
    ZusammenElement vspModel = buildStructuralElement(ElementType.VspModel, Action.CREATE);
    vspModel.addSubElement(buildOrchestrationTemplateStructure());
    vspModel.addSubElement(buildStructuralElement(ElementType.Networks, Action.CREATE));
    vspModel.addSubElement(buildStructuralElement(ElementType.Components, Action.CREATE));
    vspModel
        .addSubElement(buildStructuralElement(ElementType.ComponentDependencies, Action.CREATE));

    ZusammenElement templates = buildStructuralElement(ElementType.Templates, Action.CREATE);
    ZusammenElement artifacts = buildStructuralElement(ElementType.Artifacts, Action.CREATE);
    vspModel.addSubElement(
        buildServiceModelStructure(ElementType.ServiceModel, templates, artifacts));
    vspModel.addSubElement(
        buildServiceModelStructure(ElementType.EnrichedServiceModel, templates, artifacts));

    zusammenAdaptor.saveElement(context, elementContext, vspModel, "Create VSP Model Elements");
  }

  private ZusammenElement buildOrchestrationTemplateStructure() {
    ByteArrayInputStream emptyData = new ByteArrayInputStream(EMPTY_DATA.getBytes());

    ZusammenElement validationData =
        buildStructuralElement(ElementType.OrchestrationTemplateValidationData, Action.CREATE);
    validationData.setData(emptyData);

    ZusammenElement orchestrationTemplateStructure = buildStructuralElement(ElementType
        .OrchestrationTemplateStructure, Action.CREATE);
    validationData.setData(emptyData);

    ZusammenElement orchestrationTemplate =
        buildStructuralElement(ElementType.OrchestrationTemplate, Action.CREATE);
    orchestrationTemplate.setData(emptyData);
    orchestrationTemplate.addSubElement(validationData);
    orchestrationTemplate.addSubElement(orchestrationTemplateStructure);

    return orchestrationTemplate;
  }

  private ZusammenElement buildServiceModelStructure(ElementType serviceModelElementType,
                                                     ZusammenElement templates,
                                                     ZusammenElement artifacts) {
    ZusammenElement serviceModel = buildStructuralElement(serviceModelElementType, Action.CREATE);
    serviceModel.addSubElement(templates);
    serviceModel.addSubElement(artifacts);
    return serviceModel;
  }

  @Override
  public void update(VspDetails vspDetails) {
    ZusammenElement generalElement = mapVspDetailsToZusammenElement(vspDetails, Action.UPDATE);

    SessionContext context = createSessionContext();
    zusammenAdaptor.saveElement(context,
        new ElementContext(vspDetails.getId(), vspDetails.getVersion().getId()), generalElement,
        "Update VSP General Info Element");
  }

  @Override
  public VspDetails get(VspDetails vspDetails) {
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(vspDetails.getId(), vspDetails.getVersion().getId());
    VspDetails vsp = zusammenAdaptor.getElementInfoByName(context, elementContext, null,
        ElementType.VendorSoftwareProduct.name())
        .map(new ElementToVSPGeneralConvertor()::convert)
        .orElse(null);
    vsp.setId(vspDetails.getId());
    vsp.setVersion(vspDetails.getVersion());
    return vsp;
  }

  @Override
  public void delete(VspDetails vspDetails) {
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(vspDetails.getId(), vspDetails.getVersion().getId());

    zusammenAdaptor.saveElement(context, elementContext,
        buildStructuralElement(ElementType.VspModel, Action.DELETE),
        "Delete VSP Model Elements");

    createVspModelStructure(context, elementContext);
  }

  @Override
  public void updateQuestionnaireData(String vspId, Version version, String questionnaireData) {
    SessionContext context = createSessionContext();

    ZusammenElement questionnaireElement = mapQuestionnaireToZusammenElement(questionnaireData);
    zusammenAdaptor.saveElement(context, new ElementContext(vspId, version.getId()),
        questionnaireElement, "Update VSP Questionnaire");
  }

  @Override
  public VspQuestionnaireEntity getQuestionnaire(String vspId, Version version) {

    SessionContext context = createSessionContext();
    ElementToVSPQuestionnaireConvertor convertor = new ElementToVSPQuestionnaireConvertor();
    VspQuestionnaireEntity entity = convertor.convert(zusammenAdaptor
        .getElementByName(context, new ElementContext(vspId, version.getId()), null,
            ElementType.VSPQuestionnaire.name()).map(element -> element).orElse(null));
    entity.setId(vspId);
    entity.setVersion(version);
    return entity;
  }

  @Override
  public boolean isManual(String vspId, Version version) {
    final VspDetails vspDetails = get(new VspDetails(vspId, version));
    if (vspDetails != null && "Manual".equals(vspDetails.getOnboardingMethod())) {
      return true;
    }
    return false;
  }

  private ZusammenElement mapVspDetailsToZusammenElement(VspDetails vspDetails, Action action) {
    ZusammenElement generalElement =
        buildStructuralElement(ElementType.VendorSoftwareProduct, action);
    addVspDetailsToInfo(generalElement.getInfo(), vspDetails);
    return generalElement;
  }

  private ZusammenElement mapQuestionnaireToZusammenElement(String questionnaireData) {
    ZusammenElement questionnaireElement =
        buildStructuralElement(ElementType.VSPQuestionnaire, Action.UPDATE);
    questionnaireElement.setData(new ByteArrayInputStream(questionnaireData.getBytes()));
    return questionnaireElement;
  }

  private void addVspDetailsToInfo(Info info, VspDetails vspDetails) {
    info.addProperty(InfoPropertyName.NAME.getValue(), vspDetails.getName());
    info.addProperty(InfoPropertyName.DESCRIPTION.getValue(), vspDetails.getDescription());
    info.addProperty(InfoPropertyName.ICON.getValue(), vspDetails.getIcon());
    info.addProperty(InfoPropertyName.CATEGORY.getValue(), vspDetails.getCategory());
    info.addProperty(InfoPropertyName.SUB_CATEGORY.getValue(), vspDetails.getSubCategory());
    info.addProperty(InfoPropertyName.VENDOR_ID.getValue(), vspDetails.getVendorId());
    info.addProperty(InfoPropertyName.VENDOR_NAME.getValue(), vspDetails.getVendorName());
    if (vspDetails.getVlmVersion() != null) {
      info.addProperty(InfoPropertyName.VENDOR_VERSION.getValue(), vspDetails.getVlmVersion().getId());
    }
    info.addProperty(InfoPropertyName.LICENSE_AGREEMENT.getValue(), vspDetails.getLicenseAgreement());
    info.addProperty(InfoPropertyName.FEATURE_GROUPS.getValue(), vspDetails.getFeatureGroups());
    info.addProperty(InfoPropertyName.ON_BOARDING_METHOD.getValue(), vspDetails.getOnboardingMethod());
  }

  public enum InfoPropertyName {
    NAME("name"),
    DESCRIPTION("description"),
    ICON("icon"),
    CATEGORY("category"),
    SUB_CATEGORY("subCategory"),
    VENDOR_ID("vendorId"),
    VENDOR_NAME("vendorName"),
    VENDOR_VERSION("vendorVersion"),
    LICENSE_AGREEMENT("licenseAgreement"),
    FEATURE_GROUPS("featureGroups"),
    ON_BOARDING_METHOD("onboardingMethod");

    private String value;

    InfoPropertyName(String value){
      this.value=value;
    }

    public String getValue(){
      return value;
    }
  }

}
