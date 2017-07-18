package org.openecomp.sdc.vendorsoftwareproduct.types;


import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;

import java.util.Set;

public class ComponentValidationResult {
  private boolean valid;
  private Set<CompositionEntityValidationData> validationData;

  public ComponentValidationResult(Set<CompositionEntityValidationData> validationData) {
    this.validationData = validationData;
    valid = validationData == null;
  }

  public boolean isValid() {
    return valid;
  }

  public Set<CompositionEntityValidationData> getValidationData() {
    return validationData;
  }
}
