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
import chai, { expect } from 'chai';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { shallow } from 'enzyme';
import React from 'react';

import Breadcrumb from '../components/Breadcrumb';
import Breadcrumbs from '../components/Breadcrumbs';
import ComponentDetach from '../components/ComponentDetach';
import ComponentMeasure from '../components/ComponentMeasure';
import ComponentName from '../components/ComponentName';
import ComponentsEmpty from '../components/ComponentsEmpty';
import Truncated from '../components/Truncated';

import { getComponentUrl } from '../../../helpers/urls';
import QualifierIcon from '../../../components/shared/qualifier-icon';

chai.use(sinonChai);

const measures = [
  { metric: 'ncloc', value: 9757 }
];
const exampleComponent = {
  uuid: 'A1',
  key: 'A',
  name: 'AA',
  qualifier: 'TRK',
  measures
};
const exampleComponent2 = { uuid: 'B2', key: 'B' };
const exampleComponent3 = { uuid: 'C3', key: 'C' };
const exampleOnBrowse = sinon.spy();

describe('Code :: Components', () => {

  describe('<Breadcrumb/>', () => {
    it('should render <ComponentName/>', () => {
      const output = shallow(
          <Breadcrumb
              component={exampleComponent}
              onBrowse={exampleOnBrowse}/>
      );

      expect(output.type())
          .to.equal(ComponentName);
      expect(output.props())
          .to.deep.equal({ component: exampleComponent, onBrowse: exampleOnBrowse });
    });
  });

  describe('<Breadcrumbs/>', () => {
    let output;
    let list;

    before(() => {
      output = shallow(
          <Breadcrumbs
              breadcrumbs={[exampleComponent, exampleComponent2, exampleComponent3]}
              onBrowse={exampleOnBrowse}/>);
      list = output.find(Breadcrumb);
    });

    it('should render list of <Breadcrumb/>s', () => {
      expect(list)
          .to.have.length(3);
      expect(list.at(0).prop('component'))
          .to.equal(exampleComponent);
      expect(list.at(1).prop('component'))
          .to.equal(exampleComponent2);
      expect(list.at(2).prop('component'))
          .to.equal(exampleComponent3);
    });

    it('should pass onBrowse to all components except the last one', () => {
      expect(list.at(0).prop('onBrowse'))
          .to.equal(exampleOnBrowse);
      expect(list.at(1).prop('onBrowse'))
          .to.equal(exampleOnBrowse);
      expect(list.at(2).prop('onBrowse'))
          .to.equal(null);
    });
  });

  describe('<ComponentDetach/>', () => {
    it('should render link', () => {
      const output = shallow(
          <ComponentDetach component={exampleComponent}/>);
      const expectedUrl = getComponentUrl(exampleComponent.key);

      expect(output.type())
          .to.equal('a');
      expect(output.prop('href'))
          .to.equal(expectedUrl);
    });
  });

  describe('<ComponentMeasure/>', () => {
    it('should render formatted measure', () => {
      const output = shallow(
          <ComponentMeasure
              component={exampleComponent}
              metricKey="ncloc"
              metricType="SHORT_INT"/>);

      expect(output.text())
          .to.equal('9.8k');
    });

    it('should not render measure', () => {
      const output = shallow(
          <ComponentMeasure
              component={exampleComponent}
              metricKey="random"
              metricType="SHORT_INT"/>);

      expect(output.text())
          .to.equal('');
    });
  });

  describe('<ComponentName/>', () => {
    it('should render <QualifierIcon/>', () => {
      const output = shallow(
          <ComponentName
              component={exampleComponent}
              onBrowse={exampleOnBrowse}/>);
      const findings = output.find(QualifierIcon);

      expect(findings)
          .to.have.length(1);
      expect(findings.first().prop('qualifier'))
          .to.equal('TRK');
    });

    it('should render link to component', () => {
      const output = shallow(
          <ComponentName
              component={exampleComponent}
              onBrowse={exampleOnBrowse}/>);
      const findings = output.find('a');

      expect(findings)
          .to.have.length(1);
      expect(findings.first().text())
          .to.equal('AA');
    });

    it('should not render link to component', () => {
      const output = shallow(
          <ComponentName
              component={exampleComponent}
              onBrowse={null}/>);
      const findings = output.find('span');

      expect(output.find('a'))
          .to.have.length(0);
      expect(findings)
          .to.have.length(1);
      expect(findings.first().text())
          .to.equal('AA');
    });

    it('should browse on click', () => {
      const spy = sinon.spy();
      const preventDefaultSpy = sinon.spy();
      const output = shallow(
          <ComponentName
              component={exampleComponent}
              onBrowse={spy}/>);
      const findings = output.find('a');

      findings.first().simulate('click', { preventDefault: preventDefaultSpy });

      expect(preventDefaultSpy).to.have.been.called;
      expect(spy).to.have.been.calledWith(exampleComponent);
    });
  });

  describe('<ComponentsEmpty/>', () => {
    it('should render', () => {
      const output = shallow(<ComponentsEmpty/>);

      expect(output.text())
          .to.include('no_results');
    });
  });

  describe('<Truncated/>', () => {
    it('should render and set title', () => {
      const output = shallow(<Truncated title="ABC">123</Truncated>);

      expect(output.type())
          .to.equal('span');
      expect(output.text())
          .to.equal('123');
      expect(output.prop('title'))
          .to.equal('ABC');
    });
  });
});
