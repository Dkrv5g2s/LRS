import ComponentCard from "@/components/common/ComponentCard";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import BasicTableOne from "@/components/tables/BasicTableOne";
import { Metadata } from "next";
import React from "react";


export default function BasicTables() {
  return (
    <div>
      <PageBreadcrumb pageTitle="Reservation List" />
      <div className="space-y-6">
        
          <BasicTableOne />
        
      </div>
    </div>
  );
}
