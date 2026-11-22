import React from 'react';

export interface FieldProps {
  id: string;
  label: string;
  hint?: string;
  children: React.ReactNode;
  className?: string;
}

export function Field({ id, label, hint, children, className = '' }: FieldProps) {
  return (
    <div className={`space-y-1.5 ${className}`}>
      <label
        htmlFor={id}
        className="block text-xs font-medium text-ink cursor-pointer"
      >
        {label}
      </label>
      {children}
      {hint && <p className="text-xs text-ink-muted">{hint}</p>}
    </div>
  );
}
