import React from "react";

type Props = React.ButtonHTMLAttributes<HTMLButtonElement>;

const BlueButton: React.FC<Props> = ({ className = "", ...props }) => {
  return (
    <button
      className={`bg-primary text-text p-2 rounded-lg hover:bg-primary-pressed transition-colors duration-100 h-10 ${className}`}
      {...props}
    />
  );
};

export default BlueButton;