/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.versioning;

import java.util.List;
import org.openecomp.sdc.versioning.dao.types.Revision;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;

public interface VersioningManager {

  List<Version> list(String itemId); // TODO: 5/24/2017 filter (by status for example)

  Version get(String itemId, Version version);

  Version create(String itemId, Version version,
      VersionCreationMethod creationMethod);

  void submit(String itemId, Version version, String submitDescription);

  void publish(String itemId, Version version, String message);

  void sync(String itemId, Version version);

  void forceSync(String itemId, Version version);

  void revert(String itemId, Version version, String revisionId);

  List<Revision> listRevisions(String itemId, Version version);

  void updateVersion(String itemId, Version version);
}
