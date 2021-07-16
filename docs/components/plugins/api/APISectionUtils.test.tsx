import { render } from '@testing-library/react';
import * as React from 'react';

import { resolveTypeName } from './APISectionUtils';

describe('APISectionUtils.resolveTypeName', () => {
  test('void', () => {
    const { container } = render(<>{resolveTypeName({ type: 'intrinsic', name: 'void' })}</>);
    expect(container).toMatchSnapshot();
  });

  test('generic type', () => {
    const { container } = render(<>{resolveTypeName({ type: 'intrinsic', name: 'string' })}</>);
    expect(container).toMatchSnapshot();
  });

  test('custom type', () => {
    const { container } = render(
      <>{resolveTypeName({ type: 'reference', name: 'SpeechSynthesisEvent' })}</>
    );
    expect(container).toMatchSnapshot();
  });

  test('custom type array', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'array',
          elementType: { type: 'reference', name: 'AppleAuthenticationScope' },
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('custom type non-linkable array', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'array',
          elementType: { type: 'reference', name: 'T' },
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('query type', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'reference',
          typeArguments: [{ queryType: { type: 'reference', name: 'View' }, type: 'query' }],
          name: 'React.ComponentProps',
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('Promise', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'reference',
          typeArguments: [{ type: 'intrinsic', name: 'void' }],
          name: 'Promise',
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('Promise with custom type', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'reference',
          typeArguments: [{ type: 'reference', name: 'AppleAuthenticationCredential' }],
          name: 'Promise',
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('Record', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'reference',
          typeArguments: [
            { type: 'intrinsic', name: 'string' },
            { type: 'intrinsic', name: 'any' },
          ],
          name: 'Record',
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('Record with union', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'reference',
          typeArguments: [
            { type: 'intrinsic', name: 'string' },
            {
              type: 'union',
              types: [
                { type: 'intrinsic', name: 'number' },
                { type: 'intrinsic', name: 'boolean' },
                { type: 'intrinsic', name: 'string' },
              ],
            },
          ],
          name: 'Record',
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('union', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'union',
          types: [
            { type: 'reference', name: 'SpeechEventCallback' },
            { type: 'literal', value: null },
          ],
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('union with array', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'union',
          types: [
            { type: 'array', elementType: { type: 'intrinsic', name: 'number' } },
            { type: 'literal', value: null },
          ],
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('union with custom type and array', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'union',
          types: [
            { type: 'array', elementType: { type: 'reference', name: 'AssetRef' } },
            { type: 'reference', name: 'AssetRef' },
          ],
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('generic type', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'reference',
          typeArguments: [{ type: 'reference', name: 'Asset' }],
          name: 'PagedInfo',
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('tuple type', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'tuple',
          elements: [
            { type: 'reference', name: 'SortByKey' },
            { type: 'intrinsic', name: 'boolean' },
          ],
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('generic type in Promise', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'reference',
          typeArguments: [
            {
              type: 'reference',
              typeArguments: [{ type: 'reference', name: 'Asset' }],
              name: 'PagedInfo',
            },
          ],
          name: 'Promise',
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('function', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'reflection',
          declaration: {
            signatures: [
              {
                type: {
                  type: 'union',
                  types: [
                    { type: 'intrinsic', name: 'void' },
                    {
                      type: 'reference',
                      name: 'SpeechEventCallback',
                    },
                  ],
                },
              },
            ],
          },
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('function with arguments', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'reflection',
          declaration: {
            signatures: [
              {
                parameters: [
                  {
                    name: 'error',
                    type: { type: 'reference', name: 'Error' },
                  },
                ],
                type: {
                  type: 'union',
                  types: [
                    { type: 'intrinsic', name: 'void' },
                    { type: 'reference', name: 'SpeechEventCallback' },
                  ],
                },
              },
            ],
          },
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('function with non-linkable custom type', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'reflection',
          declaration: {
            signatures: [
              {
                parameters: [
                  {
                    name: 'error',
                    type: { type: 'reference', name: 'Error' },
                  },
                ],
                type: { type: 'intrinsic', name: 'void' },
              },
            ],
          },
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });

  test('extended props with multiple omits', () => {
    const { container } = render(
      <>
        {resolveTypeName({
          type: 'reference',
          typeArguments: [
            {
              type: 'reference',
              typeArguments: [
                { type: 'reference', name: 'ViewStyle' },
                {
                  type: 'union',
                  types: [
                    { type: 'literal', value: 'backgroundColor' },
                    {
                      type: 'literal',
                      value: 'borderRadius',
                    },
                  ],
                },
              ],
              name: 'Omit',
            },
          ],
          name: 'StyleProp',
        })}
      </>
    );
    expect(container).toMatchSnapshot();
  });
});
