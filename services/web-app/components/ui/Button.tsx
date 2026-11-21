import React from 'react';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  children: React.ReactNode;
}

export function Button({
  variant = 'primary',
  className = '',
  type = 'button',
  children,
  ...props
}: ButtonProps) {
  const baseStyles =
    'inline-flex items-center justify-center gap-2 rounded-md px-4 py-2.5 text-sm font-medium transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-moss-500 focus-visible:ring-offset-2 focus-visible:ring-offset-paper disabled:opacity-50 disabled:pointer-events-none cursor-pointer';

  const variants: Record<ButtonVariant, string> = {
    primary: 'bg-forest-900 text-white hover:bg-forest-700',
    secondary: 'bg-sun-500 text-[#1B2A22] font-semibold hover:brightness-95',
    ghost:
      'bg-transparent text-ink border border-line hover:bg-surface hover:border-forest-700',
  };

  return (
    <button
      type={type}
      className={`${baseStyles} ${variants[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
