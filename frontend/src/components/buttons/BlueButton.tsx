import React from "react";

type Props = React.ButtonHTMLAttributes<HTMLButtonElement>;

const BlueButton: React.FC<Props> = ({ className = "", ...props }) => {
  return (
    <button
      className={`bg-primary text-white font-medium rounded-lg hover:bg-primary-pressed transition-colors duration-150 h-10 px-4 ${className}`}
      {...props}
    />
  );
};

export default BlueButton;