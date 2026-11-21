import React from 'react';

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  as?: 'div' | 'article' | 'section';
}

export function Card({
  children,
  className = '',
  as: Component = 'div',
  ...props
}: CardProps) {
  return (
    <Component
      className={`rounded-xl bg-surface border border-line p-6 transition-colors duration-150 ${className}`}
      {...props}
    >
      {children}
    </Component>
  );
}
