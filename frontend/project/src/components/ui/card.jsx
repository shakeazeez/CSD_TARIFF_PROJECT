import React from 'react';

export const Card = ({ children, ...props }) => <div className="border rounded p-4" {...props}>{children}</div>;
export const CardContent = ({ children, ...props }) => <div {...props}>{children}</div>;
export const CardDescription = ({ children, ...props }) => <p className="text-gray-600" {...props}>{children}</p>;
export const CardHeader = ({ children, ...props }) => <div className="mb-2" {...props}>{children}</div>;
export const CardTitle = ({ children, ...props }) => <h3 className="font-bold" {...props}>{children}</h3>;