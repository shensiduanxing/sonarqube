/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.computation.component;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.scanner.protocol.output.ScannerReport;
import org.sonar.scanner.protocol.output.ScannerReport.Component.ComponentType;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.sonar.scanner.protocol.output.ScannerReport.Component.ComponentType.DIRECTORY;
import static org.sonar.scanner.protocol.output.ScannerReport.Component.ComponentType.FILE;
import static org.sonar.scanner.protocol.output.ScannerReport.Component.ComponentType.UNRECOGNIZED;
import static org.sonar.scanner.protocol.output.ScannerReport.Component.ComponentType.UNSET;
import static org.sonar.server.computation.component.ComponentImpl.builder;

public class ComponentImplTest {

  static final String KEY = "KEY";
  static final String UUID = "UUID";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void verify_key_and_uuid() throws Exception {
    ComponentImpl component = builder(ScannerReport.Component.newBuilder().setType(FILE).build()).setKey(KEY).setUuid(UUID).build();

    assertThat(component.getKey()).isEqualTo(KEY);
    assertThat(component.getUuid()).isEqualTo(UUID);
  }

  @Test
  public void builder_throws_NPE_if_component_arg_is_Null() {
    thrown.expect(NullPointerException.class);

    builder(null);
  }

  @Test
  public void set_key_throws_NPE_if_component_arg_is_Null() {
    thrown.expect(NullPointerException.class);

    builder(ScannerReport.Component.newBuilder().setType(FILE).build()).setUuid(null);
  }

  @Test
  public void set_uuid_throws_NPE_if_component_arg_is_Null() {
    thrown.expect(NullPointerException.class);

    builder(ScannerReport.Component.newBuilder().setType(FILE).build()).setKey(null);
  }

  @Test
  public void build_without_key_throws_NPE_if_component_arg_is_Null() {
    thrown.expect(NullPointerException.class);

    builder(ScannerReport.Component.newBuilder().setType(FILE).build()).setUuid("ABCD").build();
  }

  @Test
  public void build_without_uuid_throws_NPE_if_component_arg_is_Null() {
    thrown.expect(NullPointerException.class);

    builder(ScannerReport.Component.newBuilder().setType(FILE).build()).setKey(KEY).build();
  }

  @Test
  public void get_name_from_batch_component() {
    String name = "project";
    ComponentImpl component = buildSimpleComponent(ScannerReport.Component.newBuilder().setType(FILE).setName(name).build());
    assertThat(component.getName()).isEqualTo(name);
  }

  @Test
  public void get_version_from_batch_component() {
    String version = "1.0";
    ComponentImpl component = buildSimpleComponent(ScannerReport.Component.newBuilder().setType(FILE).setVersion(version).build());
    assertThat(component.getReportAttributes().getVersion()).isEqualTo(version);
  }

  @Test
  public void getFileAttributes_throws_ISE_if_BatchComponent_does_not_have_type_FILE() {
    for (ComponentType componentType : from(asList(ComponentType.values())).filter(not(in(asList(FILE, UNSET, UNRECOGNIZED))))) {
      ComponentImpl component = buildSimpleComponent(ScannerReport.Component.newBuilder().setType(componentType).build());
      try {
        component.getFileAttributes();
        fail("A IllegalStateException should have been raised");
      } catch (IllegalStateException e) {
        assertThat(e).hasMessage("Only component of type FILE have a FileAttributes object");
      }
    }
  }

  @Test
  public void isUnitTest_returns_true_if_IsTest_is_set_in_BatchComponent() {
    ComponentImpl component = buildSimpleComponent(ScannerReport.Component.newBuilder().setType(FILE).setIsTest(true).build());

    assertThat(component.getFileAttributes().isUnitTest()).isTrue();
  }

  @Test
  public void isUnitTest_returns_value_of_language_of_BatchComponent() {
    String languageKey = "some language key";
    ComponentImpl component = buildSimpleComponent(ScannerReport.Component.newBuilder().setType(FILE).setLanguage(languageKey).build());

    assertThat(component.getFileAttributes().getLanguageKey()).isEqualTo(languageKey);
  }

  @Test
  public void build_with_child() throws Exception {
    ComponentImpl child = builder(ScannerReport.Component.newBuilder().setType(FILE).build())
      .setKey("CHILD_KEY")
      .setUuid("CHILD_UUID")
      .build();
    ComponentImpl componentImpl = builder(ScannerReport.Component.newBuilder().setType(DIRECTORY).build())
      .setKey(KEY)
      .setUuid(UUID)
      .addChildren(child)
      .build();

    assertThat(componentImpl.getChildren()).hasSize(1);
    Component childReloaded = componentImpl.getChildren().iterator().next();
    assertThat(childReloaded.getKey()).isEqualTo("CHILD_KEY");
    assertThat(childReloaded.getUuid()).isEqualTo("CHILD_UUID");
    assertThat(childReloaded.getType()).isEqualTo(Component.Type.FILE);
  }

  @Test
  public void convertType() {
    for (ComponentType componentType : from(asList(ComponentType.values())).filter(not(in(asList(UNSET, UNRECOGNIZED))))) {
      assertThat(ComponentImpl.Builder.convertType(componentType)).isEqualTo(Component.Type.valueOf(componentType.name()));
    }
  }

  private static ComponentImpl buildSimpleComponent(ScannerReport.Component reportComponent) {
    return builder(reportComponent).setKey(KEY).setUuid(UUID).build();
  }

  @Test
  public void equals_compares_on_uuid_only() {
    ComponentImpl.Builder builder = builder(ScannerReport.Component.newBuilder().setType(FILE).build()).setUuid(UUID);

    assertThat(builder.setKey("1").build()).isEqualTo(builder.setKey("1").build());
    assertThat(builder.setKey("1").build()).isEqualTo(builder.setKey("2").build());
  }

  @Test
  public void hashCode_is_hashcode_of_uuid() {
    ComponentImpl.Builder builder = builder(ScannerReport.Component.newBuilder().setType(FILE).build()).setUuid(UUID);

    assertThat(builder.setKey("1").build().hashCode()).isEqualTo(builder.setKey("1").build().hashCode());
    assertThat(builder.setKey("1").build().hashCode()).isEqualTo(builder.setKey("2").build().hashCode());
    assertThat(builder.setKey("1").build().hashCode()).isEqualTo(UUID.hashCode());
  }
}
