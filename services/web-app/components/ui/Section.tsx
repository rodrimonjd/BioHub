import React from 'react';
import { Container } from './Container';

export interface SectionProps extends React.HTMLAttributes<HTMLElement> {
  children: React.ReactNode;
  containerClassName?: string;
}

export function Section({
  children,
  className = '',
  containerClassName = '',
  ...props
}: SectionProps) {
  return (
    <section className={`py-20 fade-in-section ${className}`} {...props}>
      <Container className={containerClassName}>{children}</Container>
    </section>
  );
}
